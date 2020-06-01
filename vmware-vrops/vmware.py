
import requests
import json
from requests.exceptions import HTTPError
import urllib3

#disable insure login
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

#login function

def login():
    headers = {'Accept': 'application/json', 'Content-Type': 'application/json'}
    payload = {'username': 'vdonkor', 'authSource': 'dev.mc2.mil','password': 'milcloud2.0'}
    url = "https://vrops-tenant-mg01-01.dev.mc2.mil/suite-api/api/auth/token/acquire"
    
    token = ""
    try:
        req = requests.post(url, headers=headers, data=json.dumps(payload),verify=False)
    
        response = req.json()
        token = response['token']

    except HTTPError as http_err:
        print("Http error occured", http_err)
    except Exception as err:
        print("Errror occured", err)
    return token


def test1(auth):
    headers = {'Accept': 'application/json', 'Content-Type': 'application/json', 'Authorization': 'vRealizeOpsToken '+ auth}
    payload = {'username': 'vdonkor', 'authSource': 'dev.mc2.mil','password': 'milcloud2.0'}
    url = "https://vrops-tenant-mg01-01.dev.mc2.mil/suite-api/api/adapterkinds/VMWARE/resourcekinds/VirtualMachine/statkeys"
    
    try:
        req = requests.get(url,headers=headers,verify=False)

        response = req.json()
    except HTTPError as http_err:
        print("Http error occured", http_err)
    except Exception as err:
        print("Errror occured", err)
    return response



# #logout
# def logout(authorization):
#     headers = {'Accept': 'application/json', 'Authorization': authorization}
#     url = "https://10.222.203.71/auth/logout"
#     try:
#         req = requests.post(url, headers=headers, verify=False)
#     except HTTPError as http_err:
#         print("Http error occured", http_err)
#     except Exception as err:
#         print("Error occured", err)
#     print(req.status_code)

#get data
def get_report(auth):
    headers = {'Accept': 'application/json', 'Authorization': 'vRealizeOpsToken '+ auth,'Content-Type': 'application/json'}
    url = "https://vrops-tenant-mg01-01.dev.mc2.mil/suite-api/api/resources"
    payload = {'resourceKind': 'virtualmachine'}
    try:
        req = requests.get(url, headers=headers,data=json.dumps(payload),verify=False)
        response = req.json()
    except HTTPError as http_err:
        print("Http error occured", http_err)
    except Exception as err:
        print("Error occured", err)
    return response

myauth = login()

print(json.dumps(test1(myauth),indent=4))
