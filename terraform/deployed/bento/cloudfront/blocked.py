import datetime
import boto3
import json
import os
from io import StringIO
from notification import send_blocked_ips_report

# list holding blocked ips
block_ip_file_name = os.getenv("BLOCK_IP_FILE_NAME")
waf_scope = os.getenv("WAF_SCOPE")
s3_bucket_name = os.getenv("S3_BUCKET_NAME")

#get previous date
previous_date = datetime.datetime.today() - datetime.timedelta(days=1)

#temporal file use to reconstruct list of blocked ip
local_blocked_ip_file_name = os.getenv("TMP_FILE_NAME")
#s3 key prefix since files are stored in days
key_prefix = '{:04d}'.format(previous_date.year) + "/" + '{:02d}'.format(previous_date.month) + "/" + '{:02d}'.format(previous_date.day)

session = boto3.session.Session()
s3 = session.resource('s3')
wafv2 = session.client('wafv2', region_name='us-east-1')

#get name of waf rule that contains blocked ip list
name_of_blocked_ip_list = os.getenv("IP_SETS_NAME")


#this function was used to create a test ip sets.
def create_blocked_ip_list(name, ips: list):
    response = wafv2.create_ip_set(
        Name=name,
        Scope=waf_scope,
        Description="List of ip blocked from cloudfront for violating files download limit",
        IPAddressVersion="IPV4",
        Addresses=ips
    )
    return response

#retrieve list of ips being blocked by waf previously. This takes name of the ip sets
def get_blocked_ip_list(name):
    id_blocked_ip_list = get_blocked_ip_list_id(name)
    response = wafv2.get_ip_set(
        Name=name,
        Scope=waf_scope,
        Id=id_blocked_ip_list
    )
    addresses = response['IPSet']['Addresses']
    lock_token = response['LockToken']
    return lock_token,addresses

#update list of ip sets returned above with ones
def update_blocked_ip_list(name, ip_addresses: list):
    id_blocked_ip_list = get_blocked_ip_list_id(name)
    lock_token, blocked_ips = get_blocked_ip_list(name)
    for i in ip_addresses:
        blocked_ips.append(i + '/32')
    wafv2.update_ip_set(
        Name=name,
        Scope=waf_scope,
        Id=id_blocked_ip_list,
        Addresses=blocked_ips,
        LockToken=lock_token
    )


#get id of the ip set by passing in the name of the ip set
def get_blocked_ip_list_id(name):
    response = wafv2.list_ip_sets(
        Scope=waf_scope
    )
    ip_sets = response['IPSets']
    for ip_set in ip_sets:
        if ip_set['Name'] == name:
            return ip_set['Id']


#function to read s3 object, it takes bucket name and key
def read_s3_object(bucket_name, key):
    s3_read_object = s3.Object(bucket_name, key)
    content = s3_read_object.get()['Body'].read().decode('utf-8')
    data = StringIO(content)
    return data.readlines()

# extract blocked ip from s3 object
def read_blocked_ips(bucket_name, key):
    ips = []
    file_contents = read_s3_object(bucket_name, key)
    for content in file_contents:
        content = json.loads(content)
        if not content["httpRequest"]["clientIp"]:
            continue
        ips.append(content["httpRequest"]["clientIp"])
    return ips

#update list of blocked in key in s3
def write_blocked_ips(bucket_name, key, blocked_ips: list):
    s3_write_object = s3.Object(bucket_name, key)
    for ip in blocked_ips:
        s3_write_object.put(Body=str(ip).encode('ascii'))

#helper function to get content of s3 object
def read_current_files(bucket_name, prefix):
    s3_files = []
    s3_objects = s3.Bucket(bucket_name)
    for s3_object in s3_objects.objects.filter(Prefix=prefix):
        s3_files.append(s3_object.key)
    return s3_files

#get list of all recently blocked ip that are not in ip sets
def get_newly_blocked_ips(bucket_name, prefix):
    blocked_ips = []
    files = read_current_files(bucket_name, prefix)
    for s3_key in files:
        blocked_ips.extend(read_blocked_ips(s3_bucket_name, s3_key))
    return list(set(blocked_ips))

#helper function to process list variable
def remove_item(items: list, item):
    for i in items:
        if i == item:
            items.remove(i)
    return items

#this is the function that processes blocked ips from log stream
def get_all_blocked_ips(bucket_name, key, prefix, file_name):
    black_listed_ips = []
    all_blocked_ips = read_s3_object(bucket_name, key)
    if all_blocked_ips:
        all_blocked_ips = [i.strip() for i in all_blocked_ips if not i == '\n' or i == '']
    new_blocked_ips = get_newly_blocked_ips(bucket_name, prefix)
    all_blocked_ips.extend(new_blocked_ips)
    for ip in all_blocked_ips:
        if all_blocked_ips.count(ip) >= 2:
            black_listed_ips.append(ip)
            all_blocked_ips = remove_item(all_blocked_ips, ip)
    if len(all_blocked_ips) >= 1:
        with open(file_name, 'w') as f:
            for line in all_blocked_ips:
                f.write(line + '\n')
    return black_listed_ips

#upload file to s3
def upload_blocked_ips(bucket_name, key, file_name):
    s3.meta.client.upload_file(file_name, bucket_name, key)
    if os.path.exists(file_name):
        os.remove(file_name)

#main lambda entry
def handler(event, context):
    waf_blocked_ips = get_all_blocked_ips(s3_bucket_name, block_ip_file_name, key_prefix, local_blocked_ip_file_name)
    upload_blocked_ips(s3_bucket_name, block_ip_file_name, local_blocked_ip_file_name)
    update_blocked_ip_list(name_of_blocked_ip_list, waf_blocked_ips)
    if waf_blocked_ips:
        send_blocked_ips_report(waf_blocked_ips)

