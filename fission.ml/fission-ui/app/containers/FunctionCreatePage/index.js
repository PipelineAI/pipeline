/*
 *
 * FunctionCreatePage
 *
 */

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import { injectIntl, intlShape, FormattedMessage } from 'react-intl';
import Helmet from 'react-helmet';
import { createStructuredSelector } from 'reselect';
import FunctionTabForm from 'components/FunctionTabForm';
import LoadingIndicator from 'components/LoadingIndicator';
import ErrorIndicator from 'components/ErrorIndicator';
import { makeSelectLoading, makeSelectError, makeSelectFunctionTest } from 'containers/FunctionsPage/selectors';
import { makeSelectEnvironments } from 'containers/EnvironmentsPage/selectors';
import { loadEnvironmentAction } from 'containers/EnvironmentsListPage/actions';
import { createFunctionAction, testFunctionAction, cleanTestFunctionAction } from 'containers/FunctionCreatePage/actions';
import { slug, encodeBase64 } from 'utils/util';
import commonMessages from 'messages';

export class FunctionCreatePage extends React.Component { // eslint-disable-line react/prefer-stateless-function
  constructor(props) {
    super(props);

    this.state = {
      loading: props.loading,
      functionTest: props.functionTest,
      error: props.error,
      inputErrors: [],
      currentTab: 'function',
      item: { name: '', environment: '', triggersHttp: [], kubeWatchers: [], code: '', temporaryFunction: '' },
      environments: props.environments,
    };

    this.onChange = this.onChange.bind(this);
    this.onSave = this.onSave.bind(this);
    this.onCodeChange = this.onCodeChange.bind(this);
    this.onFunctionTest = this.onFunctionTest.bind(this);
  }

  componentDidMount() {
    if (this.state.environments.length === 0) {
      this.props.loadEnvironmentData();
    }
    this.props.cleanTestFunction();
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.loading !== this.state.loading) {
      this.state.loading = nextProps.loading;
    }
    if (nextProps.error !== this.state.error) {
      this.state.error = nextProps.error;
    }
    if (nextProps.functionTest !== this.state.functionTest) {
      this.state.functionTest = nextProps.functionTest;
    }
    if (nextProps.environments.length !== this.state.environments.length) {
      this.state.environments = nextProps.environments;
    }
  }

  onChange(event) {
    const obj = Object.assign({}, this.state.item);
    if (event.target.name === 'name') {
      obj[event.target.name] = slug(event.target.value);
    } else {
      obj[event.target.name] = event.target.value;
    }

    this.setState({ item: obj });
  }

  onCodeChange(newValue) {
    const obj = Object.assign({}, this.state.item);
    obj.code = newValue;

    this.setState({ item: obj });
  }

  onSave() {
    const { item } = this.state;
    if (this.isFunctionRequiredInputValid(item)) {
      const fn = Object.assign({}, item);
      fn.code = encodeBase64(fn.code);
      this.props.createFunction(fn);
    }
  }

  onFunctionTest(test) {
    const fn = Object.assign({}, this.state.item);

    if (this.isFunctionRequiredInputValid(fn)) {
      fn.test = test;
      fn.code = encodeBase64(fn.code);
      this.props.testFunction(fn);
      return true;
    }
    return false;
  }

  isFunctionRequiredInputValid(item) {
    const inputErrors = [];
    const { intl } = this.props;
    if (item.name === '') {
      inputErrors.push(intl.formatMessage(commonMessages.inputErrorNeedName));
    }
    if (item.environment === '') {
      inputErrors.push(intl.formatMessage(commonMessages.inputErrorNeedEnvironment));
    }
    if (item.code === '') {
      inputErrors.push(intl.formatMessage(commonMessages.inputErrorNeedCode));
    }
    window.scrollTo(0, 0);
    this.setState({ inputErrors });
    return inputErrors.length === 0;
  }

  render() {
    const { item, environments, loading, error, inputErrors, functionTest } = this.state;
    if (loading) {
      return <LoadingIndicator />;
    }
    return (
      <div>
        <Helmet
          title="Create function"
        />

        {error &&
          <ErrorIndicator errors={[error.response.data]} />
        }
        {inputErrors.length > 0 &&
          <ErrorIndicator errors={inputErrors} />
        }

        <FunctionTabForm
          item={item}
          environments={environments}
          onChange={this.onChange}
          metadataEditable={Boolean(true)}
          onCodeChange={this.onCodeChange}
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

FunctionCreatePage.propTypes = {
  environments: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.array,
  ]),
  loading: PropTypes.bool,
  error: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.bool,
  ]),
  loadEnvironmentData: PropTypes.func.isRequired,
  createFunction: PropTypes.func.isRequired,
  testFunction: PropTypes.func.isRequired,
  cleanTestFunction: PropTypes.func.isRequired,
  functionTest: PropTypes.object,
  intl: intlShape.isRequired,
};

const mapStateToProps = createStructuredSelector({
  environments: makeSelectEnvironments(),
  loading: makeSelectLoading(),
  error: makeSelectError(),
  functionTest: makeSelectFunctionTest(),
});

function mapDispatchToProps(dispatch) {
  return {
    loadEnvironmentData: () => dispatch(loadEnvironmentAction()),
    createFunction: (fn) => dispatch(createFunctionAction(fn)),
    testFunction: (fn) => dispatch(testFunctionAction(fn)),
    cleanTestFunction: () => dispatch(cleanTestFunctionAction()),
  };
}

export default injectIntl(connect(mapStateToProps, mapDispatchToProps)(FunctionCreatePage));
