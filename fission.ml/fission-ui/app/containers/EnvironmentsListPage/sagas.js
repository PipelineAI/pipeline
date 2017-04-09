import { takeLatest, call, put, take, cancel } from 'redux-saga/effects';
import { getEnvironments, removeEnvironment } from 'utils/api';
import { LOCATION_CHANGE } from 'react-router-redux';
import { LOAD_ENVIRONMENTS_REQUEST, LOAD_ENVIRONMENTS_SUCCESS, LOAD_ENVIRONMENTS_ERROR, DELETE_ENVIRONMENT_REQUEST, DELETE_ENVIRONMENT_SUCCESS, DELETE_ENVIRONMENT_ERROR } from 'containers/EnvironmentsPage/constants';

function* loadEnvironments() {
  try {
    const data = yield call(getEnvironments);
    yield put({ type: LOAD_ENVIRONMENTS_SUCCESS, data });
  } catch (error) {
    yield put({ type: LOAD_ENVIRONMENTS_ERROR, error });
  }
}

function* removeEnvironmentSaga(action) {
  try {
    yield call(removeEnvironment, action.environment);
    yield put({ type: DELETE_ENVIRONMENT_SUCCESS, environment: action.environment });
  } catch (error) {
    yield put({ type: DELETE_ENVIRONMENT_ERROR, error });
  }
}

// Individual exports for testing
export function* getAllSaga() {
  const watcher = yield takeLatest(LOAD_ENVIRONMENTS_REQUEST, loadEnvironments);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}
export function* removeSaga() {
  const watcher = yield takeLatest(DELETE_ENVIRONMENT_REQUEST, removeEnvironmentSaga);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}

// All sagas to be loaded
export default [
  getAllSaga,
  removeSaga,
];
