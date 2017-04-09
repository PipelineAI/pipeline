/*
 *
 * FunctionEditPage
 *
 */

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import { FormattedMessage } from 'react-intl';
import Helmet from 'react-helmet';
import { createStructuredSelector } from 'reselect';
import FunctionForm from 'components/FunctionForm';
import LoadingIndicator from 'components/LoadingIndicator';
import ErrorIndicator from 'components/ErrorIndicator';
import { makeSelectLoading, makeSelectFunctionByName, makeSelectTriggersHttp, makeSelectError, makeSelectFunctionTest, makeSelectKubeWatchers } from 'containers/FunctionsPage/selectors';
import { makeSelectEnvironments } from 'containers/EnvironmentsPage/selectors';
import { loadEnvironmentAction } from 'containers/EnvironmentsListPage/actions';
import { testFunctionAction, cleanTestFunctionAction } from 'containers/FunctionCreatePage/actions';
import { getFunctionAction, loadTriggersHttpAction, deleteTriggerHttpAction, updateFunctionAction, createTriggerHttpAction, loadKubeWatchersAction, createKubeWatcherAction, deleteKubeWatcherAction } from 'containers/FunctionEditPage/actions';
import commonMessages from 'messages';
import { encodeBase64 } from 'utils/util';

export class FunctionEditPage extends React.Component { // eslint-disable-line react/prefer-stateless-function
  constructor(props) {
    super(props);

    const hash = this.props.location.hash.replace('#', '');
    // must use once for es lint errors
    props.functionByName();
    this.state = {
      loading: props.loading,
      functionTest: props.functionTest,
      error: props.error,
      inputErrors: [],
      environments: props.environments,
      httpTriggers: props.httpTriggers,
      kubeWatchers: props.kubeWatchers,
      activeTab: hash === '' ? 'function' : hash,
      editing: false,
    };
    this.onChange = this.onChange.bind(this);
    this.onSave = this.onSave.bind(this);
    this.onHttpTriggerRemove = this.onHttpTriggerRemove.bind(this);
    this.onHttpTriggerCreate = this.onHttpTriggerCreate.bind(this);
    this.onKubeWatcherRemove = this.onKubeWatcherRemove.bind(this);
    this.onKubeWatcherCreate = this.onKubeWatcherCreate.bind(this);
    this.onCodeChange = this.onCodeChange.bind(this);
    this.onTabChange = this.onTabChange.bind(this);
    this.onFunctionTest = this.onFunctionTest.bind(this);
  }

  componentDidMount() {
    if (this.state.environments.length === 0) {
      this.props.loadEnvironmentData();
    }
    if (this.state.httpTriggers.length === 0) {
      this.props.loadTriggersHttpData();
    }
    if (this.state.kubeWatchers.length === 0) {
      this.props.loadKubeWatchersData();
    }
    this.props.loadFunctionData(this.props.params.name);
    this.props.cleanTestFunction();
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.loading !== this.state.loading) {
      this.state.loading = nextProps.loading;
    }
    if (nextProps.functionTest !== this.state.functionTest) {
      this.state.functionTest = nextProps.functionTest;
    }
    if (nextProps.error !== this.state.error) {
      this.state.error = nextProps.error;
    }
    if (nextProps.httpTriggers.length !== this.state.httpTriggers.length) {
      this.state.httpTriggers = nextProps.httpTriggers;
    }
    if (nextProps.kubeWatchers.length !== this.state.kubeWatchers.length) {
      this.state.kubeWatchers = nextProps.kubeWatchers;
    }
    if (nextProps.environments.length !== this.state.environments.length) {
      this.state.environments = nextProps.environments;
    }
    const nextState = nextProps.functionByName(nextProps.params.name);
    if (nextState !== false) {
      if (!this.state.editing) {
        this.state.item = nextState;
      } else {
        this.state.item.triggersHttp = nextState.triggersHttp;
        this.state.item.kubeWatchers = nextState.kubeWatchers;
      }
    }
  }


  onChange(event) {
    const obj = Object.assign({}, this.state.item);
    obj[event.target.name] = event.target.value;

    this.setState({ item: obj, editing: true });
  }

  onCodeChange(newValue) {
    const obj = Object.assign({}, this.state.item);
    obj.code = newValue;

    this.setState({ item: obj, editing: true });
  }

  onFunctionTest(test) {
    const fn = Object.assign({}, this.state.item);
    fn.code = encodeBase64(fn.code);
    fn.test = test;
    this.props.testFunction(fn);
    return true;
  }

  onHttpTriggerRemove(item) {
    this.props.deleteTriggerHttp(item);
  }

  onHttpTriggerCreate(tr) {
    const { item } = this.state;
    const trigger = Object.assign({}, tr);
    if (!trigger.urlpattern.startsWith('/')) {
      trigger.urlpattern = `/${trigger.urlpattern}`;
    }
    this.props.createTriggerHttp({
      method: trigger.method,
      urlpattern: trigger.urlpattern,
      function: item.name,
    });
  }

  onKubeWatcherRemove(watcher) {
    this.props.deleteKubeWatcher(watcher);
  }

  onKubeWatcherCreate(watcher) {
    const { item } = this.state;
    const obj = Object.assign({}, watcher);
    obj.function = item.name;
    this.props.createKubeWatcher(obj);
  }

  onTabChange(newTabName) {
    this.setState({ activeTab: newTabName });
  }

  onSave(event) {
    event.preventDefault();
    const { item } = this.state;
    const fn = Object.assign({}, item);
    fn.code = encodeBase64(fn.code);
    this.props.updateFunction(fn);
  }

  render() {
    const { item, environments, loading, error, activeTab, functionTest } = this.state;
    if (loading || item === undefined) {
      return <LoadingIndicator />;
    }
    return (
      <div>
        <Helmet
          title="Edit function"
        />

        {error &&
          <ErrorIndicator errors={[error.response.data]} />
        }

        <FunctionForm
          environments={environments} onChange={this.onChange} item={item}
          onHttpTriggerRemove={this.onHttpTriggerRemove}
          onHttpTriggerCreate={this.onHttpTriggerCreate}
          onKubeWatcherRemove={this.onKubeWatcherRemove}
          onKubeWatcherCreate={this.onKubeWatcherCreate}
          metadataEditable={Boolean(false)}
          onCodeChange={this.onCodeChange}
          activeTab={activeTab}
          onTabChange={this.onTabChange}
          onFunctionTest={this.onFunctionTest}
          functionTest={functionTest}
        />

        <div className="pull-right">
          <a className="btn btn-primary" onClick={this.onSave}><FormattedMessage {...commonMessages.deploy} /></a> { ' ' }
          <Link to="/" className="btn btn-default"><FormattedMessage {...commonMessages.cancel} /></Link>
        </div>
      </div>
    );
  }
}
FunctionEditPage.propTypes = {
  environments: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.array,
  ]),
  httpTriggers: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.array,
  ]),
  kubeWatchers: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.array,
  ]),
  loading: PropTypes.bool,
  error: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.bool,
  ]),
  location: PropTypes.object,
  functionByName: PropTypes.func.isRequired,
  loadEnvironmentData: PropTypes.func.isRequired,
  loadFunctionData: PropTypes.func.isRequired,
  loadTriggersHttpData: PropTypes.func.isRequired,
  loadKubeWatchersData: PropTypes.func.isRequired,
  deleteTriggerHttp: PropTypes.func.isRequired,
  updateFunction: PropTypes.func.isRequired,
  createTriggerHttp: PropTypes.func.isRequired,
  createKubeWatcher: PropTypes.func.isRequired,
  deleteKubeWatcher: PropTypes.func.isRequired,
  params: PropTypes.object.isRequired,
  testFunction: PropTypes.func.isRequired,
  cleanTestFunction: PropTypes.func.isRequired,
  functionTest: PropTypes.object.isRequired,
};

const mapStateToProps = createStructuredSelector({
  functionByName: makeSelectFunctionByName(),
  environments: makeSelectEnvironments(),
  httpTriggers: makeSelectTriggersHttp(),
  kubeWatchers: makeSelectKubeWatchers(),
  loading: makeSelectLoading(),
  error: makeSelectError(),
  functionTest: makeSelectFunctionTest(),
});

function mapDispatchToProps(dispatch) {
  return {
    loadEnvironmentData: () => dispatch(loadEnvironmentAction()),
    loadTriggersHttpData: () => dispatch(loadTriggersHttpAction()),
    loadKubeWatchersData: () => dispatch(loadKubeWatchersAction()),
    loadFunctionData: (name) => dispatch(getFunctionAction(name)),
    deleteTriggerHttp: (trigger) => dispatch(deleteTriggerHttpAction(trigger)),
    updateFunction: (fn) => dispatch(updateFunctionAction(fn)),
    createTriggerHttp: (trigger) => dispatch(createTriggerHttpAction(trigger)),
    testFunction: (fn) => dispatch(testFunctionAction(fn)),
    cleanTestFunction: () => dispatch(cleanTestFunctionAction()),
    createKubeWatcher: (watcher) => dispatch(createKubeWatcherAction(watcher)),
    deleteKubeWatcher: (watcher) => dispatch(deleteKubeWatcherAction(watcher)),
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(FunctionEditPage);
