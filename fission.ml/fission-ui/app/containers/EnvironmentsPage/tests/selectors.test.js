import { fromJS } from 'immutable';
import {
  makeSelectEnvironmentByName,
  makeSelectEnvironments,
  makeSelectError,
  makeSelectLoading,
} from '../selectors';

const mockedState = fromJS({
  environments: {
    environments: [
      {
        metadata: {
          name: 'node',
          uid: 'a9b76a1f-ee1f-4177-83ee-2c9a016a5da3',
        },
        runContainerImageUrl: 'fission/node-env',
      },
      {
        metadata: {
          name: 'python',
          uid: '830e152b-771d-4cc5-8e3e-ea323b698aa7',
        },
        runContainerImageUrl: 'fission/python-env',
      },
    ],
    loading: false,
    error: false,
  },
});

describe('makeSelectEnvironmentByName', () => {
  it('should select the existed python environment', () => {
    expect(makeSelectEnvironmentByName()(mockedState)('python'))
      .toEqual({ image: 'fission/python-env', name: 'python' });
  });
  it('should return false as the non-existed php environment', () => {
    expect(makeSelectEnvironmentByName()(mockedState)('php'))
      .toEqual(false);
  });
});

describe('makeSelectEnvironments', () => {
  it('should select the environment list', () => {
    expect(makeSelectEnvironments()(mockedState))
      .toEqual([
        {
          name: 'node',
          image: 'fission/node-env',
        },
        {
          name: 'python',
          image: 'fission/python-env',
        },
      ]);
  });
  it('should select the empty environment list', () => {
    expect(makeSelectEnvironments()(mockedState.setIn(['environments', 'environments'], fromJS([]))))
      .toEqual([]);
  });
});

describe('makeSelectLoading', () => {
  it('should select the loading', () => {
    expect(makeSelectLoading()(mockedState))
      .toEqual(false);
  });
});

describe('makeSelectError', () => {
  it('should select the error', () => {
    expect(makeSelectError()(mockedState))
      .toEqual(false);
  });
});
