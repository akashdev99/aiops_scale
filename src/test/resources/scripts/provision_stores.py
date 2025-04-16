from opentelemetry.sdk.metrics.export import MetricsData, MetricExportResult
from requests.adapters import HTTPAdapter
from urllib3.util import Retry
import requests
import os
import pandas as pd

def post(endpoint,token, payload=None, expected_return_code=200):
    try:
        print(f"Sending POST request to {endpoint} with payload {payload} and token {token}")
        retry = Retry(
            total=3,
            backoff_factor=2,
            status_forcelist=[i for i in range(400, 600)],
        )
        adapter = HTTPAdapter(max_retries=retry)
        session = requests.Session()
        session.mount("https://", adapter)
        response = session.post(
            endpoint,
            data=payload,
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token ,
            },
            timeout=180,
        )
        print("Response: ", response)
        assert (
            response.status_code == expected_return_code
        ), f"POST request to {endpoint} failed with status code {response.status_code}"
        return response
    except Exception as e:
        print(f"Failed to send POST request to {endpoint} with payload {payload}")
        raise e

if __name__=="__main__":
    data = pd.read_csv("../api_user_tokens.csv")

    for i , token in enumerate(data["API_USER_TOKEN"].tolist()):
        print("index",i)
        post("https://edge.scale.cdo.cisco.com/api/platform/ai-ops-tenant-services/v1/timeseries-stack", token=token , payload=None, expected_return_code=202)