/*
 *
 * EnvironmentEditPage
 *
 */

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import { createStructuredSelector } from 'reselect';
import EnvironmentForm from 'components/EnvironmentForm';
import LoadingIndicator from 'components/LoadingIndicator';
import ErrorIndicator from 'components/ErrorIndicator';
import { slug } from 'utils/util';

import { makeSelectEnvironmentByName, makeSelectError, makeSelectLoading } from 'containers/EnvironmentsPage/selectors';
import { getEnvironmentAction, editEnvironmentAction } from './actions';

export class EnvironmentEditPage extends React.Component { // eslint-disable-line react/prefer-stateless-function
  constructor(props) {
    super(props);
    this.state = {
      loading: props.loading,
      error: props.error,
      environment: props.environmentByName(props.params.name),
    };
    this.submitForm = this.submitForm.bind(this);
    this.onChange = this.onChange.bind(this);
  }

  componentDidMount() {
    if (!this.state.environment) {
      this.props.getEnvironment(this.props.params.name);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.loading !== this.state.loading) {
      this.state.loading = nextProps.loading;
    }
    if (nextProps.error !== this.state.error) {
      this.state.error = nextProps.error;
    }
    if (!this.state.environment) {
      this.state.environment = nextProps.environmentByName(nextProps.params.name);
    }
  }

  onChange(event) {
    const field = event.target.name;
    const environment = this.state.environment;
    environment[field] = event.target.value;
    return this.setState({ environment });
  }

  submitForm(event) {
    event.preventDefault();
    this.state.environment.name = slug(this.state.environment.name);
    this.props.editEnvironment(this.state.environment);
    return false;
  }

  render() {
    const { loading, error, environment } = this.state;

    if (loading) {
      return <LoadingIndicator />;
    }

    return (
      <div>
        <Helmet
          title="Edit environment"
        />
        {error &&
          <ErrorIndicator errors={[error.response.data]} />
        }
        {loading === false && environment &&
          <EnvironmentForm nameEditable={Boolean(false)} sampleEnabled={Boolean(false)} environment={environment} onChange={this.onChange} onSave={this.submitForm} />
        }
      </div>
    );
  }
}

EnvironmentEditPage.propTypes = {
  environmentByName: PropTypes.func,
  getEnvironment: PropTypes.func,
  editEnvironment: PropTypes.func,
  params: PropTypes.any,
  loading: PropTypes.bool,
  error: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.bool,
  ]),
};

const mapStateToProps = createStructuredSelector({
  environmentByName: makeSelectEnvironmentByName(),
  loading: makeSelectLoading(),
  error: makeSelectError(),
});

function mapDispatchToProps(dispatch) {
  return {
    getEnvironment: (name) => dispatch(getEnvironmentAction(name)),
    editEnvironment: (environment) => dispatch(editEnvironmentAction(environment)),
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(EnvironmentEditPage);
