import pandas as pd
import cloudpickle as pickle
from io import BytesIO, StringIO

__version__ = "0.1"

def csv_to_df(csv: bytes) -> pd.DataFrame:
    return pd.read_csv(BytesIO(csv))

def df_to_csv(df: pd.DataFrame) -> bytes:
    buf = StringIO()
    df.to_csv(buf, index=False)
    return buf.getvalue().encode('utf-8')
