/*
 *
 * EnvironmentsPage reducer
 *
 */

import { fromJS } from 'immutable';
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
  GET_FUNCTION_REQUEST,
  GET_FUNCTION_ERROR,
  GET_FUNCTION_SUCCESS,
  DELETE_TRIGGERHTTP_REQUEST,
  DELETE_TRIGGERHTTP_SUCCESS,
  DELETE_TRIGGERHTTP_ERROR,
  UPDATE_FUNCTION_REQUEST,
  UPDATE_FUNCTION_ERROR,
  UPDATE_FUNCTION_SUCCESS,
  CREATE_TRIGGERHTTP_REQUEST,
  CREATE_TRIGGERHTTP_ERROR,
  CREATE_TRIGGERHTTP_SUCCESS,
  CREATE_FUNCTION_REQUEST,
  CREATE_FUNCTION_ERROR,
  CREATE_FUNCTION_SUCCESS,
  TEST_FUNCTION_REQUEST,
  TEST_FUNCTION_ERROR,
  TEST_FUNCTION_SUCCESS,
  LOAD_KUBEWATCHERS_REQUEST,
  LOAD_KUBEWATCHERS_ERROR,
  LOAD_KUBEWATCHERS_SUCCESS,
  CLEAN_TEST_FUNCTION_REQUEST,
  CREATE_KUBEWATCHER_REQUEST,
  CREATE_KUBEWATCHER_ERROR,
  CREATE_KUBEWATCHER_SUCCESS,
  DELETE_KUBEWATCHER_REQUEST,
  DELETE_KUBEWATCHER_ERROR,
  DELETE_KUBEWATCHER_SUCCESS,
  SET_UPLOAD_FUNCTIONS,
  UPLOAD_FUNCTIONS_IN_BATCH_REQUEST,
  UPLOAD_SINGLE_FUNCTION_IN_BATCH_PROGRESS,
  UPLOAD_SINGLE_FUNCTION_IN_BATCH_ERROR,
} from './constants';

const initialState = fromJS({
  functions: [],
  triggersHttp: [],
  kubeWatchers: [],
  functionLoading: false,
  triggerHttpLoading: false,
  kubeWatcherLoading: false,
  functionTest: { loading: false, response: {} },
  error: false,
  uploadFunctions: [],
});

function functionsReducer(state = initialState, action) {
  // TODO simplify the switch logic
  switch (action.type) {
    case GET_FUNCTION_REQUEST:
    case UPDATE_FUNCTION_REQUEST:
    case CREATE_FUNCTION_REQUEST:
      return state
        .set('functionLoading', true)
        .set('error', false);
    case GET_FUNCTION_SUCCESS:
      return getFunctionSuccessHandler(state, action);
    case DELETE_FUNCTION_SUCCESS:
      return state
        .set('functionLoading', false)
        .set('functions', state.get('functions').filter((e) => e.getIn(['metadata', 'name']) !== action.function.name));
    case DELETE_FUNCTION_REQUEST:
      return state
        .set('functionLoading', true)
        .set('error', false);
    case LOAD_FUNCTIONS_REQUEST:
      return state
        .set('functionLoading', true)
        .set('error', false)
        .set('functions', fromJS([]));
    case LOAD_TRIGGERSHTTP_REQUEST:
      return state
        .set('triggerHttpLoading', true)
        .set('error', false)
        .set('triggersHttp', fromJS([]));
    case LOAD_FUNCTIONS_ERROR:
    case DELETE_FUNCTION_ERROR:
    case UPDATE_FUNCTION_ERROR:
    case CREATE_FUNCTION_ERROR:
    case GET_FUNCTION_ERROR:
      return state
        .set('error', fromJS(action.error))
        .set('functionLoading', false);
    case LOAD_TRIGGERSHTTP_SUCCESS:
      return state
        .set('triggersHttp', fromJS(action.data))
        .set('triggerHttpLoading', false);
    case LOAD_FUNCTIONS_SUCCESS:
      return state
        .set('functions', fromJS(action.data))
        .set('functionLoading', false);
    case DELETE_TRIGGERHTTP_REQUEST:
    case CREATE_TRIGGERHTTP_REQUEST:
      return state.set('triggerHttpLoading', true);
    case DELETE_TRIGGERHTTP_SUCCESS:
      return state
        .set('triggerHttpLoading', false)
        .update('triggersHttp', (triggers) => triggers.filter((e) => e.getIn(['metadata', 'name']) !== action.data.metadata.name));
    case LOAD_TRIGGERSHTTP_ERROR:
    case DELETE_TRIGGERHTTP_ERROR:
    case CREATE_TRIGGERHTTP_ERROR:
      return state
        .set('error', fromJS(action.error))
        .set('triggerHttpLoading', false);
    case UPDATE_FUNCTION_SUCCESS:
      // TODO update the function in the store
      // but it is easier just reload the whole function list or reload the function edit page
      return state
        .set('error', false)
        .set('functionLoading', false);
    case CREATE_FUNCTION_SUCCESS:
      return state
        .set('error', false)
        .set('functionLoading', false);
    case CREATE_TRIGGERHTTP_SUCCESS:
      return state
        .set('error', false)
        .set('triggerHttpLoading', false)
        .update('triggersHttp', (triggers) => triggers.push(fromJS(action.data)));
    case TEST_FUNCTION_REQUEST:
      return state
        .setIn(['functionTest', 'loading'], true)
        .setIn(['functionTest', 'response'], fromJS({}))
        .set('error', false);
    case TEST_FUNCTION_ERROR:
      return state
        .setIn(['functionTest', 'loading'], false)
        .set('error', fromJS(action.error));
    case TEST_FUNCTION_SUCCESS:
      return state
        .setIn(['functionTest', 'loading'], false)
        .setIn(['functionTest', 'response'], fromJS(action.data))
        .set('error', false);
    case CLEAN_TEST_FUNCTION_REQUEST:
      return state
        .setIn(['functionTest', 'loading'], false)
        .setIn(['functionTest', 'response'], fromJS({}))
        .set('error', false);
    case LOAD_KUBEWATCHERS_REQUEST:
      return state
        .set('error', false)
        .set('kubeWatcherLoading', true)
        .set('kubeWatchers', fromJS([]));
    case LOAD_KUBEWATCHERS_ERROR:
    case CREATE_KUBEWATCHER_ERROR:
    case DELETE_KUBEWATCHER_ERROR:
      return state
        .set('error', fromJS(action.error))
        .set('kubeWatcherLoading', false);
    case LOAD_KUBEWATCHERS_SUCCESS:
      return state
        .set('error', false)
        .set('kubeWatcherLoading', false)
        .set('kubeWatchers', fromJS(action.data));
    case CREATE_KUBEWATCHER_REQUEST:
    case DELETE_KUBEWATCHER_REQUEST:
      return state
        .set('error', false)
        .set('kubeWatcherLoading');
    case CREATE_KUBEWATCHER_SUCCESS:
      return state
        .set('error', false)
        .set('kubeWatcherLoading', false)
        .update('kubeWatchers', (watchers) => watchers.push(fromJS(action.data)));
    case DELETE_KUBEWATCHER_SUCCESS:
      return state
        .set('error', false)
        .set('kubeWatcherLoading', false)
        .update('kubeWatchers', (watchers) => watchers.filter((e) => e.getIn(['metadata', 'name']) !== action.data.metadata.name));
    case SET_UPLOAD_FUNCTIONS:
      return state
        .set('uploadFunctions', fromJS(action.data));
    case UPLOAD_FUNCTIONS_IN_BATCH_REQUEST:
      return state;
    case UPLOAD_SINGLE_FUNCTION_IN_BATCH_PROGRESS:
    case UPLOAD_SINGLE_FUNCTION_IN_BATCH_ERROR:
      return state
        .update('uploadFunctions', (fns) => fns.map((f) => f.get('name') === action.data.name ? fromJS(action.data) : f));
    default:
      return state;
  }
}

function getFunctionSuccessHandler(state, action) {
  const functionAlreadyExist = state.get('functions').filter((e) => e.getIn(['metadata', 'name']) === action.data.metadata.name);
  if (functionAlreadyExist.size === 1) {
    return state
      .update('functions', (fns) => fns.map((fn) => fn.getIn(['metadata', 'name']) === action.data.metadata.name ? fromJS(action.data) : fn))
      .set('functionLoading', false);
  }
  return state
    .set('functions', fromJS([action.data]))
    .set('functionLoading', false);
}

export default functionsReducer;
