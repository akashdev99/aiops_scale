import uuid

import pandas as pd

if __name__ == "__main__":
    n = 1000  # Change this to however many rows you need
    # Generate a list of random UUIDs
    uuids = [str(uuid.uuid4()) for _ in range(n)]

    # Create the DataFrame
    df = pd.DataFrame({'device_uuid': uuids})

    df.to_csv("../device_uuids.csv", index=False)