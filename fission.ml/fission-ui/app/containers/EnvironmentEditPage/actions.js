/*
 *
 * EnvironmentEditPage actions
 *
 */

import {
  GET_ENVIRONMENT_REQUEST,
  EDIT_ENVIRONMENT_REQUEST,
} from 'containers/EnvironmentsPage/constants';

export function getEnvironmentAction(name) {
  return {
    type: GET_ENVIRONMENT_REQUEST,
    name,
  };
}

export function editEnvironmentAction(environment) {
  return {
    type: EDIT_ENVIRONMENT_REQUEST,
    environment,
  };
}

