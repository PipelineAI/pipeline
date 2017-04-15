/*
 *
 * EnvironmentsListPage actions
 *
 */

import {
  LOAD_ENVIRONMENTS_REQUEST,
  DELETE_ENVIRONMENT_REQUEST,
} from 'containers/EnvironmentsPage/constants';

export function removeEnvironmentAction(environment) {
  return {
    type: DELETE_ENVIRONMENT_REQUEST,
    environment,
  };
}
export function loadEnvironmentAction() {
  return {
    type: LOAD_ENVIRONMENTS_REQUEST,
  };
}
