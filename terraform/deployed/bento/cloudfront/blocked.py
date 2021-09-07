import datetime
import boto3
import json
import os
from io import StringIO
from slack import send_slack_message

# list holding blocked ips
block_ip_file_name = 'blocked_ip/ips.txt'
s3_bucket_name = 'bento-cloudfront-kinesis-firehose-stream'
# d = datetime.datetime.today() - datetime.timedelta(days=3)
d = datetime.datetime.today()
local_blocked_ip_file_name = 'blocked_ip.txt'
key_prefix = '{:04d}'.format(d.year) + "/" + '{:02d}'.format(d.month) + "/" + '{:02d}'.format(d.day)
session = boto3.session.Session(profile_name='bento')
s3 = session.resource('s3')


def read_s3_object(bucket_name, key):
    s3_read_object = s3.Object(bucket_name, key)
    content = s3_read_object.get()['Body'].read().decode('utf-8')
    data = StringIO(content)
    return data.readlines()


def read_blocked_ips(bucket_name, key):
    ips = []
    file_contents = read_s3_object(bucket_name, key)
    for content in file_contents:
        content = json.loads(content)
        ips.append(content["httpRequest"]["clientIp"])
    return list(set(ips))


def write_blocked_ips(bucket_name, key, blocked_ips: list):
    s3_write_object = s3.Object(bucket_name, key)
    for ip in blocked_ips:
        s3_write_object.put(Body=str(ip).encode('ascii'))


def read_current_files(bucket_name, prefix):
    s3_files = []
    s3_objects = s3.Bucket(bucket_name)
    for s3_object in s3_objects.objects.filter(Prefix=prefix):
        s3_files.append(s3_object.key)
    return s3_files


def get_newly_blocked_ips(bucket_name, prefix):
    blocked_ips = []
    files = read_current_files(bucket_name, prefix)
    for s3_key in files:
        blocked_ips.extend(read_blocked_ips(s3_bucket_name, s3_key))
    return list(set(blocked_ips))


def get_all_blocked_ips(bucket_name, key, prefix, file_name):
    old_blocked_ips = read_s3_object(bucket_name, key)
    newly_blocked_ips = []
    old_blocked_ips = [i for i in old_blocked_ips if i]
    blocked_ips = get_newly_blocked_ips(bucket_name, prefix)
    for ip in blocked_ips:
        if ip not in old_blocked_ips:
            newly_blocked_ips.append(ip)
            old_blocked_ips.append(ip)
    with open(file_name, 'w') as f:
        for line in old_blocked_ips:
            f.write(line + '\n')
    return newly_blocked_ips


def upload_blocked_ips(bucket_name, key, file_name):
    s3.meta.client.upload_file(file_name, bucket_name, key)
    if os.path.exists(file_name):
        os.remove(file_name)


def handler(event, context):
    waf_blocked_ips = get_all_blocked_ips(s3_bucket_name, block_ip_file_name, key_prefix, local_blocked_ip_file_name)
    upload_blocked_ips(s3_bucket_name, block_ip_file_name, local_blocked_ip_file_name)
    send_slack_message(waf_blocked_ips)
