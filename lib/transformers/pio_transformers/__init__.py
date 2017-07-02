import pandas as pd
from io import BytesIO, StringIO
import numpy as np

__version__ = "0.2"

def csv_to_df(csv: bytes) -> pd.DataFrame:
    return pd.read_csv(BytesIO(csv))

def df_to_csv(df: pd.DataFrame) -> bytes:
    buf = StringIO()
    df.to_csv(buf, index=False)
    return buf.getvalue().encode('utf-8')
