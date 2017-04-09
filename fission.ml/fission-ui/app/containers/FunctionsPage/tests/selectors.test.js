import { fromJS } from 'immutable';
import {
  makeSelectFunctions,
  makeSelectTriggersHttp,
  makeSelectError,
  makeSelectLoading,
  makeSelectFunctionByName,
  makeSelectFunctionTest,
  makeSelectKubeWatchers,
} from '../selectors';

const mockTriggersHttp = [
  {
    metadata: {
      name: 'cf3918bf-75fe-47e1-9eea-8de5238773dc',
      uid: '9df6170e-2e7e-481d-85c6-7d6dc3b63383',
    },
    urlpattern: '/path/to/testing',
    method: 'GET',
    function: {
      name: 'testing',
    },
  },
];

const mockKubeWatchers = [
  {
    metadata: {
      name: 'cd7b0c3b-c8c2-41cc-89b2-9b5949c52236',
      uid: '6f923d94-c9de-42c2-ac2c-e02ca2518b80',
    },
    namespace: 'default',
    objtype: 'pod',
    labelselector: '',
    fieldselector: '',
    function: {
      name: 'testing',
    },
    target: '/fission-function/testing',
  },
];

const mockFunctionTest = {
  loading: false,
  response: {},
};

const mockState = fromJS({
  functions: {
    functions: [
      {
        metadata: {
          name: 'temp',
          uid: '0e1b66b3-03f3-4dd4-be48-4147b3d98ef0',
        },
        environment: {
          name: 'node',
        },
        code: '',
      },
      {
        metadata: {
          name: 'testing',
          uid: 'b123f58e-cd27-4725-9b24-966113aa8ca1',
        },
        environment: {
          name: 'node',
        },
        code: '',
      },
    ],
    triggersHttp: mockTriggersHttp,
    kubeWatchers: mockKubeWatchers,
    functionLoading: false,
    triggerHttpLoading: false,
    kubeWatcherLoading: false,
    functionTest: mockFunctionTest,
    error: false,
  },
  environments: {
    loading: false,
  },
});

describe('makeSelectFunctions', () => {
  it('should select the function list', () => {
    const functionList = [
      {
        environment: 'node',
        kubeWatchers: [],
        name: 'temp',
        triggersHttp: [],
      },
      {
        environment: 'node',
        kubeWatchers: [{ fieldselector: '', function: { name: 'testing' }, labelselector: '', metadata: { name: 'cd7b0c3b-c8c2-41cc-89b2-9b5949c52236', uid: '6f923d94-c9de-42c2-ac2c-e02ca2518b80' }, namespace: 'default', objtype: 'pod', target: '/fission-function/testing' }],
        name: 'testing',
        triggersHttp: [{ function: { name: 'testing' }, metadata: { name: 'cf3918bf-75fe-47e1-9eea-8de5238773dc', uid: '9df6170e-2e7e-481d-85c6-7d6dc3b63383' }, method: 'GET', urlpattern: '/path/to/testing' }],
      },
    ];
    expect(makeSelectFunctions()(mockState))
      .toEqual(functionList);
  });
});

describe('makeSelectTriggersHttp', () => {
  it('should select the trigger http list', () => {
    expect(makeSelectTriggersHttp()(mockState))
      .toEqual(mockTriggersHttp);
  });
});

describe('makeSelectError', () => {
  it('should select error', () => {
    expect(makeSelectError()(mockState))
      .toEqual(false);
  });
});

describe('makeSelectLoading', () => {
  it('should select the loading', () => {
    expect(makeSelectLoading()(mockState))
      .toEqual(false);
  });
});

describe('makeSelectFunctionByName', () => {
  it('should select the exist function temp', () => {
    expect(makeSelectFunctionByName()(mockState)('temp'))
      .toEqual({
        code: '',
        environment: 'node',
        kubeWatchers: [],
        name: 'temp',
        triggersHttp: [],
        uid: '0e1b66b3-03f3-4dd4-be48-4147b3d98ef0',
      });
  });
  it('should select false for the non-exist function unknown', () => {
    expect(makeSelectFunctionByName()(mockState)('unknown'))
      .toEqual(false);
  });
});

describe('makeSelectFunctionTest', () => {
  it('should select the function test', () => {
    expect(makeSelectFunctionTest()(mockState))
      .toEqual(mockFunctionTest);
  });
});

describe('makeSelectKubeWatchers', () => {
  it('should select the kube watchers list', () => {
    expect(makeSelectKubeWatchers()(mockState))
      .toEqual(mockKubeWatchers);
  });
});
