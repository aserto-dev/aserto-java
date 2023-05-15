[![slack](https://img.shields.io/badge/slack-Aserto%20Community-brightgreen)](https://asertocommunity.slack.com)


## 0. prerequisites

Ensure you are in the `examples/todo-example` directory.

```bash
cd examples/todo-example
```

## 1. building the examples

To build the example package, execute the following commands:

```bash
mvn clean package
```

## 2. setup environment

## 2.1 using [topaz](https://topaz.sh)

### 2.1.1 install and configure [topaz](https://topaz.sh)

* Install [topaz](https://github.com/aserto-dev/topaz#installation)
* Configure topaz to use the `todo` policy

```bash
topaz configure -d -s -r ghcr.io/aserto-policies/policy-todo:v2 todo
```

* Download topaz directory data

```bash
topaz stop 
wget https://raw.githubusercontent.com/aserto-dev/topaz/main/pkg/testing/assets/eds-citadel.db -O ~/.config/topaz/db/directory.db
```

* Start topaz

```bash
topaz start

```

* Validate if topaz is running

```bash
topaz status
```

## 3. Running the example

To run the examples, execute:

```bash
java -jar target/examples-1.0.0-shaded.jar
```

Run the fallowing commands to test the example:

### Create todo
```bash
curl --location 'localhost:8500/api/todo' \
--header 'Authorization: Beare eyJhbGciOiJSUzI1NiIsImtpZCI6IjVhZjM2MTI3YTk2MWEyM2IwMDAyZGNiM2NhODljODYyYzQyOTllZjQifQ.eyJpc3MiOiJodHRwczovL2NpdGFkZWwuZGVtby5hc2VydG8uY29tL2RleCIsInN1YiI6IkNpUm1aREEyTVRSa015MWpNemxoTFRRM09ERXRZamRpWkMwNFlqazJaalZoTlRFd01HUVNCV3h2WTJGcyIsImF1ZCI6ImNpdGFkZWwtYXBwIiwiZXhwIjoxNjgzODgwMDUxLCJpYXQiOjE2ODM3OTM2NTEsIm5vbmNlIjoiNTdmOGFjZDI1YmNkNGVlYmEwYzY5OTg4Mzc0NzJiMzkiLCJhdF9oYXNoIjoiSG1mcl9TaW9Zd1Y3OXNlMzlUNVplZyIsImVtYWlsIjoicmlja0B0aGUtY2l0YWRlbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6InJpY2sifQ.G3o2EFU0dd6Jca261iMA8HsU3Tm5Y762U837UipcYjfC2LFf3dX_buTZxCiGu7Y1PFwV1cV4KVSkVl2qER2BCFtdT4KkKj_OpTPD8OTxCLMGefXKeRD-K91tJN82xsw_XGCofF_Gz317FYLkccCbU0qsYgv-KyIQnPDJzi7ai7V2SN6zeW7fnACsaM-z4SSQkD14gtSsL0RDP7EixkycX0tk9eXMowRqB141JiInWLsPCqPqiESr67BtjjWR8o7qHP1qBRgdUee0pXhfyXWKPgXEbQ1bbU5p5-XQCQX7FAnm7Q-O4D57y3Id7TwCzWO1gNnCfK6W6xmVdFwGTpTmcA' \
--header 'Content-Type: application/json' \
--data '{
    "ID": "id-test",
    "Title": "todo-test",
    "Completed": false,
    "OwnerID": "sub-test"
}'
```

### Get todos
```bash
curl --location 'localhost:8500/api/todos' \
--header 'Authorization: Beare eyJhbGciOiJSUzI1NiIsImtpZCI6IjVhZjM2MTI3YTk2MWEyM2IwMDAyZGNiM2NhODljODYyYzQyOTllZjQifQ.eyJpc3MiOiJodHRwczovL2NpdGFkZWwuZGVtby5hc2VydG8uY29tL2RleCIsInN1YiI6IkNpUm1aREEyTVRSa015MWpNemxoTFRRM09ERXRZamRpWkMwNFlqazJaalZoTlRFd01HUVNCV3h2WTJGcyIsImF1ZCI6ImNpdGFkZWwtYXBwIiwiZXhwIjoxNjgzODgwMDUxLCJpYXQiOjE2ODM3OTM2NTEsIm5vbmNlIjoiNTdmOGFjZDI1YmNkNGVlYmEwYzY5OTg4Mzc0NzJiMzkiLCJhdF9oYXNoIjoiSG1mcl9TaW9Zd1Y3OXNlMzlUNVplZyIsImVtYWlsIjoicmlja0B0aGUtY2l0YWRlbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6InJpY2sifQ.G3o2EFU0dd6Jca261iMA8HsU3Tm5Y762U837UipcYjfC2LFf3dX_buTZxCiGu7Y1PFwV1cV4KVSkVl2qER2BCFtdT4KkKj_OpTPD8OTxCLMGefXKeRD-K91tJN82xsw_XGCofF_Gz317FYLkccCbU0qsYgv-KyIQnPDJzi7ai7V2SN6zeW7fnACsaM-z4SSQkD14gtSsL0RDP7EixkycX0tk9eXMowRqB141JiInWLsPCqPqiESr67BtjjWR8o7qHP1qBRgdUee0pXhfyXWKPgXEbQ1bbU5p5-XQCQX7FAnm7Q-O4D57y3Id7TwCzWO1gNnCfK6W6xmVdFwGTpTmcA'```
```

### Update todo
```bash
curl --location --request PUT 'localhost:8500/api/todo/CiRmZDE2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs' \
--header 'Authorization: Beare eyJhbGciOiJSUzI1NiIsImtpZCI6IjVhZjM2MTI3YTk2MWEyM2IwMDAyZGNiM2NhODljODYyYzQyOTllZjQifQ.eyJpc3MiOiJodHRwczovL2NpdGFkZWwuZGVtby5hc2VydG8uY29tL2RleCIsInN1YiI6IkNpUm1aREEyTVRSa015MWpNemxoTFRRM09ERXRZamRpWkMwNFlqazJaalZoTlRFd01HUVNCV3h2WTJGcyIsImF1ZCI6ImNpdGFkZWwtYXBwIiwiZXhwIjoxNjgzODgwMDUxLCJpYXQiOjE2ODM3OTM2NTEsIm5vbmNlIjoiNTdmOGFjZDI1YmNkNGVlYmEwYzY5OTg4Mzc0NzJiMzkiLCJhdF9oYXNoIjoiSG1mcl9TaW9Zd1Y3OXNlMzlUNVplZyIsImVtYWlsIjoicmlja0B0aGUtY2l0YWRlbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6InJpY2sifQ.G3o2EFU0dd6Jca261iMA8HsU3Tm5Y762U837UipcYjfC2LFf3dX_buTZxCiGu7Y1PFwV1cV4KVSkVl2qER2BCFtdT4KkKj_OpTPD8OTxCLMGefXKeRD-K91tJN82xsw_XGCofF_Gz317FYLkccCbU0qsYgv-KyIQnPDJzi7ai7V2SN6zeW7fnACsaM-z4SSQkD14gtSsL0RDP7EixkycX0tk9eXMowRqB141JiInWLsPCqPqiESr67BtjjWR8o7qHP1qBRgdUee0pXhfyXWKPgXEbQ1bbU5p5-XQCQX7FAnm7Q-O4D57y3Id7TwCzWO1gNnCfK6W6xmVdFwGTpTmcA' \
--header 'Content-Type: application/json' \
--data '{
    "ID": "id-test",
    "Title": "todo-test",
    "Completed": true,
    "OwnerID": "sub-test"
}'
```

### Delete todo
```bash
curl --location --request DELETE 'localhost:8500/api/todo/CiRmZDE2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs' \
--header 'Authorization: Beare eyJhbGciOiJSUzI1NiIsImtpZCI6IjVhZjM2MTI3YTk2MWEyM2IwMDAyZGNiM2NhODljODYyYzQyOTllZjQifQ.eyJpc3MiOiJodHRwczovL2NpdGFkZWwuZGVtby5hc2VydG8uY29tL2RleCIsInN1YiI6IkNpUm1aREEyTVRSa015MWpNemxoTFRRM09ERXRZamRpWkMwNFlqazJaalZoTlRFd01HUVNCV3h2WTJGcyIsImF1ZCI6ImNpdGFkZWwtYXBwIiwiZXhwIjoxNjgzODgwMDUxLCJpYXQiOjE2ODM3OTM2NTEsIm5vbmNlIjoiNTdmOGFjZDI1YmNkNGVlYmEwYzY5OTg4Mzc0NzJiMzkiLCJhdF9oYXNoIjoiSG1mcl9TaW9Zd1Y3OXNlMzlUNVplZyIsImVtYWlsIjoicmlja0B0aGUtY2l0YWRlbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6InJpY2sifQ.G3o2EFU0dd6Jca261iMA8HsU3Tm5Y762U837UipcYjfC2LFf3dX_buTZxCiGu7Y1PFwV1cV4KVSkVl2qER2BCFtdT4KkKj_OpTPD8OTxCLMGefXKeRD-K91tJN82xsw_XGCofF_Gz317FYLkccCbU0qsYgv-KyIQnPDJzi7ai7V2SN6zeW7fnACsaM-z4SSQkD14gtSsL0RDP7EixkycX0tk9eXMowRqB141JiInWLsPCqPqiESr67BtjjWR8o7qHP1qBRgdUee0pXhfyXWKPgXEbQ1bbU5p5-XQCQX7FAnm7Q-O4D57y3Id7TwCzWO1gNnCfK6W6xmVdFwGTpTmcA' \
--header 'Content-Type: application/json' \
--data '{
    "ID": "id-test",
    "Title": "todo-test",
    "Completed": true,
    "OwnerID": "sub-test"
}'
```

### Get user
```bash
curl --location 'localhost:8500/api/user/CiRmZDE2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs' \
--header 'Authorization: Beare eyJhbGciOiJSUzI1NiIsImtpZCI6IjVhZjM2MTI3YTk2MWEyM2IwMDAyZGNiM2NhODljODYyYzQyOTllZjQifQ.eyJpc3MiOiJodHRwczovL2NpdGFkZWwuZGVtby5hc2VydG8uY29tL2RleCIsInN1YiI6IkNpUm1aREEyTVRSa015MWpNemxoTFRRM09ERXRZamRpWkMwNFlqazJaalZoTlRFd01HUVNCV3h2WTJGcyIsImF1ZCI6ImNpdGFkZWwtYXBwIiwiZXhwIjoxNjgzODgwMDUxLCJpYXQiOjE2ODM3OTM2NTEsIm5vbmNlIjoiNTdmOGFjZDI1YmNkNGVlYmEwYzY5OTg4Mzc0NzJiMzkiLCJhdF9oYXNoIjoiSG1mcl9TaW9Zd1Y3OXNlMzlUNVplZyIsImVtYWlsIjoicmlja0B0aGUtY2l0YWRlbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6InJpY2sifQ.G3o2EFU0dd6Jca261iMA8HsU3Tm5Y762U837UipcYjfC2LFf3dX_buTZxCiGu7Y1PFwV1cV4KVSkVl2qER2BCFtdT4KkKj_OpTPD8OTxCLMGefXKeRD-K91tJN82xsw_XGCofF_Gz317FYLkccCbU0qsYgv-KyIQnPDJzi7ai7V2SN6zeW7fnACsaM-z4SSQkD14gtSsL0RDP7EixkycX0tk9eXMowRqB141JiInWLsPCqPqiESr67BtjjWR8o7qHP1qBRgdUee0pXhfyXWKPgXEbQ1bbU5p5-XQCQX7FAnm7Q-O4D57y3Id7TwCzWO1gNnCfK6W6xmVdFwGTpTmcA'```