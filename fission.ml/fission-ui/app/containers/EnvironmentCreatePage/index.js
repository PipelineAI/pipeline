/*
 *
 * EnvironmentCreatePage
 *
 */

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import { createStructuredSelector } from 'reselect';
import { injectIntl, intlShape } from 'react-intl';
import EnvironmentForm from 'components/EnvironmentForm';
import LoadingIndicator from 'components/LoadingIndicator';
import ErrorIndicator from 'components/ErrorIndicator';
import { slug } from 'utils/util';
import { makeSelectError, makeSelectLoading } from 'containers/EnvironmentsPage/selectors';
import commonMessages from 'messages';
import { createEnvironmentAction } from './actions';

export class EnvironmentCreatePage extends React.Component { // eslint-disable-line react/prefer-stateless-function
  constructor(props) {
    super();
    this.state = {
      loading: props.loading,
      error: props.error,
      inputErrors: [],
      environment: { name: '', image: '' },
    };
    this.submitForm = this.submitForm.bind(this);
    this.onChange = this.onChange.bind(this);
    this.onSelectSample = this.onSelectSample.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.loading !== this.state.loading) {
      this.state.loading = nextProps.loading;
    }
    if (nextProps.error !== this.state.error) {
      this.state.error = nextProps.error;
    }
  }

  onChange(event) {
    const field = event.target.name;
    const environment = this.state.environment;
    environment[field] = event.target.value;
    return this.setState({ environment });
  }

  onSelectSample(event) {
    if (!(event.target.value in this.environmentSamples)) {
      return false;
    }
    return this.setState({
      environment: this.environmentSamples[event.target.value],
    });
  }

  submitForm(event) {
    event.preventDefault();
    const item = this.state.environment;
    if (this.isEnvironmentRequiredInputValid(item)) {
      this.state.environment.name = slug(item.name);
      this.props.createEnvironement(item);
    }
    return false;
  }

  isEnvironmentRequiredInputValid(item) {
    const inputErrors = [];
    const { intl } = this.props;
    if (item.name === '') {
      inputErrors.push(intl.formatMessage(commonMessages.inputErrorNeedName));
    }
    if (item.image === '') {
      inputErrors.push(intl.formatMessage(commonMessages.inputErrorNeedDockerImage));
    }

    this.setState({ inputErrors });
    return inputErrors.length === 0;
  }

  environmentSamples = {
    blank: {
      name: '',
      image: '',
    },
    node: {
      name: 'node',
      image: 'fission/node-env',
    },
    python: {
      name: 'python',
      image: 'fission/python-env',
    },
  };

  render() {
    const { loading, error, inputErrors, environment } = this.state;
    if (loading) {
      return <LoadingIndicator />;
    }
    return (
      <div>
        <Helmet
          title="Environment creation"
        />
        {error &&
          <ErrorIndicator errors={[error.response.data]} />
        }
        {inputErrors.length > 0 &&
          <ErrorIndicator errors={inputErrors} />
        }
        <EnvironmentForm nameEditable={Boolean(true)} sampleEnabled={Boolean(true)} environment={environment} onChange={this.onChange} onSave={this.submitForm} onSelectSample={this.onSelectSample} />
      </div>
    );
  }
}

EnvironmentCreatePage.propTypes = {
  createEnvironement: PropTypes.func.isRequired,
  loading: PropTypes.bool,
  error: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.bool,
  ]),
  intl: intlShape.isRequired,
};

const mapStateToProps = createStructuredSelector({
  loading: makeSelectLoading(),
  error: makeSelectError(),
});

function mapDispatchToProps(dispatch) {
  return {
    createEnvironement: (environment) => dispatch(createEnvironmentAction(environment)),
  };
}

export default injectIntl(connect(mapStateToProps, mapDispatchToProps)(EnvironmentCreatePage));
