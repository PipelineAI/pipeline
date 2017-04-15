/*
 *
 * EnvironmentCreatePage actions
 *
 */

import {
  CREATE_ENVIRONMENT_REQUEST,
} from 'containers/EnvironmentsPage/constants';

export function createEnvironmentAction(environment) {
  return {
    type: CREATE_ENVIRONMENT_REQUEST,
    environment,
  };
}
