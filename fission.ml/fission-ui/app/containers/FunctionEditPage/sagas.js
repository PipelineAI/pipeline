import { take, call, put, cancel, takeLatest } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import v4 from 'uuid';
import { getFunction, removeTriggerHttp, putFunction, postTriggerHttp, postFunction, restRequest, removeFunction, removeKubeWatcher, postKubeWatcher } from 'utils/api';
import {
  GET_FUNCTION_REQUEST,
  GET_FUNCTION_SUCCESS,
  GET_FUNCTION_ERROR,
  DELETE_TRIGGERHTTP_REQUEST,
  DELETE_TRIGGERHTTP_SUCCESS,
  DELETE_TRIGGERHTTP_ERROR,
  UPDATE_FUNCTION_REQUEST,
  UPDATE_FUNCTION_SUCCESS,
  UPDATE_FUNCTION_ERROR,
  CREATE_TRIGGERHTTP_REQUEST,
  CREATE_TRIGGERHTTP_SUCCESS,
  CREATE_TRIGGERHTTP_ERROR,
  TEST_FUNCTION_REQUEST,
  TEST_FUNCTION_SUCCESS,
  TEST_FUNCTION_ERROR,
  CREATE_KUBEWATCHER_REQUEST,
  CREATE_KUBEWATCHER_ERROR,
  CREATE_KUBEWATCHER_SUCCESS,
  DELETE_KUBEWATCHER_REQUEST,
  DELETE_KUBEWATCHER_ERROR,
  DELETE_KUBEWATCHER_SUCCESS,
} from 'containers/FunctionsPage/constants';
import { LOCATION_CHANGE } from 'react-router-redux';


function* getFunctionSagaRequest(action) {
  try {
    const data = yield call(getFunction, action.name);
    yield put({ type: GET_FUNCTION_SUCCESS, data });
  } catch (error) {
    yield put({ type: GET_FUNCTION_ERROR, error });
  }
}
function* deleteTriggerHttp(action) {
  try {
    yield call(removeTriggerHttp, action.trigger);
    yield put({ type: DELETE_TRIGGERHTTP_SUCCESS, data: action.trigger });
  } catch (error) {
    yield put({ type: DELETE_TRIGGERHTTP_ERROR, error });
  }
}
function* updateFunction(action) {
  try {
    yield call(putFunction, action.fn);

    // force to reload the updated function again
    yield put({ type: GET_FUNCTION_REQUEST, name: action.fn.name });

    yield put({ type: UPDATE_FUNCTION_SUCCESS, data: action.fn });
  } catch (error) {
    yield put({ type: UPDATE_FUNCTION_ERROR, error });
  }
}
function* createTriggerHttp(action) {
  try {
    const item = action.trigger;
    const trigger = { metadata: { name: v4() }, method: item.method, urlpattern: item.urlpattern, function: { name: item.function } };
    yield call(postTriggerHttp, trigger);

    yield put({ type: CREATE_TRIGGERHTTP_SUCCESS, data: trigger });
  } catch (error) {
    yield put({ type: CREATE_TRIGGERHTTP_ERROR, error });
  }
}
function* testFunction(action) {
  const { fn } = action;
  const { method, headers, params, body, draft } = fn.test;
  if (draft) {
    fn.name = v4();
  }
  const url = `/fission-function/${fn.name}`;

  try {
    if (draft) {
      yield call(postFunction, fn);
      yield delay(4 * 1000);
    }
    const data = yield call(restRequest, url, method, headers, params, body);
    if (draft) {
      yield call(removeFunction, fn);
    }

    yield put({ type: TEST_FUNCTION_SUCCESS, data });
  } catch (error) {
    yield put({ type: TEST_FUNCTION_ERROR, error });
  }
}
function* createKubeWatcher(action) {
  try {
    const item = action.watcher;
    const watcher = { metadata: { name: v4() }, namespace: item.namespace, objtype: item.objtype, labelselector: item.labelselector, function: { name: item.function } };
    yield call(postKubeWatcher, watcher);

    yield put({ type: CREATE_KUBEWATCHER_SUCCESS, data: watcher });
  } catch (error) {
    yield put({ type: CREATE_KUBEWATCHER_ERROR, error });
  }
}
function* deleteKubeWatcher(action) {
  try {
    yield call(removeKubeWatcher, action.watcher);

    yield put({ type: DELETE_KUBEWATCHER_SUCCESS, data: action.watcher });
  } catch (error) {
    yield put({ type: DELETE_KUBEWATCHER_ERROR, error });
  }
}


export function* getFunctionSaga() {
  // See example in containers/HomePage/sagas.js
  const watcher = yield takeLatest(GET_FUNCTION_REQUEST, getFunctionSagaRequest);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}
export function* removeTriggerHttpSaga() {
  const watcher = yield takeLatest(DELETE_TRIGGERHTTP_REQUEST, deleteTriggerHttp);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}
export function* updateFunctionSaga() {
  const watcher = yield takeLatest(UPDATE_FUNCTION_REQUEST, updateFunction);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}
export function* createTriggerHttpSaga() {
  const watcher = yield takeLatest(CREATE_TRIGGERHTTP_REQUEST, createTriggerHttp);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}
export function* testFunctionSaga() {
  const watcher = yield takeLatest(TEST_FUNCTION_REQUEST, testFunction);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}
export function* createKubeWatcherSaga() {
  const watcher = yield takeLatest(CREATE_KUBEWATCHER_REQUEST, createKubeWatcher);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}
export function* deleteKubeWatcherSaga() {
  const watcher = yield takeLatest(DELETE_KUBEWATCHER_REQUEST, deleteKubeWatcher);

  // Suspend execution until location changes
  yield take(LOCATION_CHANGE);
  yield cancel(watcher);
}

// All sagas to be loaded
export default [
  getFunctionSaga,
  removeTriggerHttpSaga,
  updateFunctionSaga,
  createTriggerHttpSaga,
  testFunctionSaga,
  createKubeWatcherSaga,
  deleteKubeWatcherSaga,
];
