import {
  LOAD_FUNCTIONS_REQUEST,
  LOAD_TRIGGERSHTTP_REQUEST,
  DELETE_FUNCTION_REQUEST,
  LOAD_KUBEWATCHERS_REQUEST,
} from 'containers/FunctionsPage/constants';


export function loadFunctionAction() {
  return {
    type: LOAD_FUNCTIONS_REQUEST,
  };
}

export function loadTriggersHttpAction() {
  return {
    type: LOAD_TRIGGERSHTTP_REQUEST,
  };
}

export function loadKubeWatchersAction() {
  return {
    type: LOAD_KUBEWATCHERS_REQUEST,
  };
}

export function deleteFunctionAction(func) {
  return {
    type: DELETE_FUNCTION_REQUEST,
    function: func,
  };
}
