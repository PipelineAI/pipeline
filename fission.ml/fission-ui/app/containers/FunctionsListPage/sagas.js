// import { take, call, put, select } from 'redux-saga/effects';
import { takeLatest, call, put, take, cancel } from 'redux-saga/effects';
import { getFunctions, getTriggersHttp, removeFunction, removeTriggerHttp, getKubeWatchers, removeKubeWatcher } from 'utils/api';
import { LOCATION_CHANGE } from 'react-router-redux';
import {
  LOAD_FUNCTIONS_REQUEST,
  LOAD_FUNCTIONS_SUCCESS,
  LOAD_FUNCTIONS_ERROR,
  DELETE_FUNCTION_REQUEST,
  DELETE_FUNCTION_SUCCESS,
  DELETE_FUNCTION_ERROR,
  LOAD_TRIGGERSHTTP_REQUEST,
  LOAD_TRIGGERSHTTP_SUCCESS,
  LOAD_TRIGGERSHTTP_ERROR,
  DELETE_TRIGGERHTTP_ERROR,
  LOAD_KUBEWATCHERS_REQUEST,
  LOAD_KUBEWATCHERS_SUCCESS,
  LOAD_KUBEWATCHERS_ERROR,
  DELETE_KUBEWATCHER_ERROR,
} from 'containers/FunctionsPage/constants';

function* loadFunctions() {
  try {
    const data = yield call(getFunctions);
    yield put({ type: LOAD_FUNCTIONS_SUCCESS, data });
  } catch (error) {
    yield put({ type: LOAD_FUNCTIONS_ERROR, error });
  }
}
function* loadTriggerHttp() {
  try {
    const data = yield call(getTriggersHttp);
    yield put({ type: LOAD_TRIGGERSHTTP_SUCCESS, data });
  } catch (error) {
    yield put({ type: LOAD_TRIGGERSHTTP_ERROR, error });
  }
}
function* deleteFunction(action) {
  const triggers = action.function.triggersHttp;
  for (let i = 0; i < triggers.length; i += 1) {
    const trigger = triggers[i];
    try {
      yield call(removeTriggerHttp, trigger);
    } catch (error) {
      yield put({ type: DELETE_TRIGGERHTTP_ERROR, error });
    }
  }
  const watchers = action.function.kubeWatchers;
  for (let i = 0; i < watchers.length; i += 1) {
    const watcher = watchers[i];
    try {
      yield call(removeKubeWatcher, watcher);
    } catch (error) {
      yield put({ type: DELETE_KUBEWATCHER_ERROR, error });
    }
  }

  try {
    yield call(removeFunction, action.function);
    yield put({ type: DELETE_FUNCTION_SUCCESS, function: action.function });
  } catch (error) {
    yield put({ type: DELETE_FUNCTION_ERROR, error });
  }
}
function* loadKubeWatchers() {
  try {
    const data = yield call(getKubeWatchers);
    yield put({ type: LOAD_KUBEWATCHERS_SUCCESS, data });
  } catch (error) {
    yield put({ type: LOAD_KUBEWATCHERS_ERROR, error });
  }
}

export function* getAllFunctionsSaga() {
  const watcher = yield takeLatest(LOAD_FUNCTIONS_REQUEST, loadFunctions);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}
export function* getAllTriggersHttpSaga() {
  const watcher = yield takeLatest(LOAD_TRIGGERSHTTP_REQUEST, loadTriggerHttp);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}
export function* removeFunctionSaga() {
  const watcher = yield takeLatest(DELETE_FUNCTION_REQUEST, deleteFunction);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}
export function* getAllKubeWatchersSaga() {
  const watcher = yield takeLatest(LOAD_KUBEWATCHERS_REQUEST, loadKubeWatchers);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}

// All sagas to be loaded
export default [
  getAllFunctionsSaga,
  getAllTriggersHttpSaga,
  removeFunctionSaga,
  getAllKubeWatchersSaga,
];
