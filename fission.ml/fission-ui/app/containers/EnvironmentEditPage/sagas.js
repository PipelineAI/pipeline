import { takeLatest, call, put, take, cancel } from 'redux-saga/effects';
import { LOCATION_CHANGE } from 'react-router-redux';
import { getEnvironment, updateEnvironment } from 'utils/api';
import { GET_ENVIRONMENT_REQUEST, GET_ENVIRONMENT_SUCCESS, GET_ENVIRONMENT_ERROR, EDIT_ENVIRONMENT_REQUEST, EDIT_ENVIRONMENT_SUCCESS, EDIT_ENVIRONMENT_ERROR } from 'containers/EnvironmentsPage/constants';
import { browserHistory } from 'react-router';

// Individual exports for testing
function* getEnvironmentSagaRequest(action) {
  try {
    const data = yield call(getEnvironment, action.name);
    yield put({ type: GET_ENVIRONMENT_SUCCESS, data });
  } catch (error) {
    yield put({ type: GET_ENVIRONMENT_ERROR, error });
  }
}

function* editEnvironmentSagaRequest(action) {
  try {
    const data = yield call(updateEnvironment, action.environment);
    yield put({ type: EDIT_ENVIRONMENT_SUCCESS, data });
    browserHistory.push('/environments');
  } catch (error) {
    yield put({ type: EDIT_ENVIRONMENT_ERROR, error });
  }
}

// Individual exports for testing
export function* getEnvironmentSaga() {
  // See example in containers/HomePage/sagas.js
  const watcher = yield takeLatest(GET_ENVIRONMENT_REQUEST, getEnvironmentSagaRequest);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}

export function* editEnvironmentSaga() {
  // See example in containers/HomePage/sagas.js
  const watcher = yield takeLatest(EDIT_ENVIRONMENT_REQUEST, editEnvironmentSagaRequest);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}

// All sagas to be loaded
export default [
  getEnvironmentSaga,
  editEnvironmentSaga,
];
