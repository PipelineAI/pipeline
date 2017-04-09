import {
  UPLOAD_FUNCTIONS_IN_BATCH_REQUEST,
  SET_UPLOAD_FUNCTIONS,
} from 'containers/FunctionsPage/constants';

export function uploadFunctionsInBatchAction(fns, isCreate) {
  return {
    type: UPLOAD_FUNCTIONS_IN_BATCH_REQUEST,
    fns,
    isCreate,
  };
}

export function setUploadFunctionsAction(fns) {
  return {
    type: SET_UPLOAD_FUNCTIONS,
    data: fns,
  };
}
