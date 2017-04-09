
import { takeLatest, call, put, take, cancel } from 'redux-saga/effects';
import { LOCATION_CHANGE } from 'react-router-redux';
import { createEnvironment } from 'utils/api';
import { CREATE_ENVIRONMENT_REQUEST, CREATE_ENVIRONMENT_SUCCESS, CREATE_ENVIRONMENT_ERROR } from 'containers/EnvironmentsPage/constants';
import { browserHistory } from 'react-router';

// Individual exports for testing
function* createEnvironmentSagaRequest(action) {
  try {
    const data = yield call(createEnvironment, action.environment);
    yield put({ type: CREATE_ENVIRONMENT_SUCCESS, data });
    browserHistory.push('/environments');
  } catch (error) {
    yield put({ type: CREATE_ENVIRONMENT_ERROR, error });
  }
}

// Individual exports for testing
export function* defaultSaga() {
  // See example in containers/HomePage/sagas.js
  const watcher = yield takeLatest(CREATE_ENVIRONMENT_REQUEST, createEnvironmentSagaRequest);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}

// All sagas to be loaded
export default [
  defaultSaga,
];
