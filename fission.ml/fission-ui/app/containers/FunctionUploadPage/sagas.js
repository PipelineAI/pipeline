import { take, call, put, cancel, takeLatest } from 'redux-saga/effects';
import { putFunction, postFunction } from 'utils/api';
import {
  UPLOAD_FUNCTIONS_IN_BATCH_REQUEST,
  UPLOAD_SINGLE_FUNCTION_IN_BATCH_PROGRESS,
  UPLOAD_SINGLE_FUNCTION_IN_BATCH_ERROR,
} from 'containers/FunctionsPage/constants';
import { LOCATION_CHANGE } from 'react-router-redux';

function* uploadFunctions(action) {
  const apiFunction = action.isCreate ? postFunction : putFunction;
  for (let i = 0; i < action.fns.length; i += 1) {
    const fn = action.fns[i];
    try {
      fn.status = 'processing';
      yield put({ type: UPLOAD_SINGLE_FUNCTION_IN_BATCH_PROGRESS, data: Object.assign({}, fn) });
      yield call(apiFunction, fn);
      fn.status = 'uploaded';
      yield put({ type: UPLOAD_SINGLE_FUNCTION_IN_BATCH_PROGRESS, data: Object.assign({}, fn) });
    } catch (error) {
      fn.errors = error.response ? [error.response.data] : [JSON.stringify(error)];
      fn.status = 'failed';
      yield put({ type: UPLOAD_SINGLE_FUNCTION_IN_BATCH_ERROR, data: Object.assign({}, fn) });
    }
  }
}

export function* uploadFunctionsSaga() {
  const watcher = yield takeLatest(UPLOAD_FUNCTIONS_IN_BATCH_REQUEST, uploadFunctions);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}

// All sagas to be loaded
export default [
  uploadFunctionsSaga,
];
