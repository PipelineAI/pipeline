import {
  CREATE_FUNCTION_REQUEST,
  TEST_FUNCTION_REQUEST,
  CLEAN_TEST_FUNCTION_REQUEST,
} from 'containers/FunctionsPage/constants';

export function createFunctionAction(fn) {
  return {
    type: CREATE_FUNCTION_REQUEST,
    fn,
  };
}

export function testFunctionAction(fn) {
  return {
    type: TEST_FUNCTION_REQUEST,
    fn,
  };
}

export function cleanTestFunctionAction() {
  return {
    type: CLEAN_TEST_FUNCTION_REQUEST,
  };
}
