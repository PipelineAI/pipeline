/*
 *
 * EnvironmentsListPage
 *
 */

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import { FormattedMessage } from 'react-intl';
import Helmet from 'react-helmet';
import { Link } from 'react-router';
import { createStructuredSelector } from 'reselect';
import EnvironmentsList from 'components/EnvironmentsList';
import { makeSelectEnvironments, makeSelectError, makeSelectLoading } from 'containers/EnvironmentsPage/selectors';
import commonMessages from 'messages';
import { loadEnvironmentAction, removeEnvironmentAction } from './actions';

export class EnvironmentsListPage extends React.Component { // eslint-disable-line react/prefer-stateless-function
  constructor() {
    super();
    this.onRemove = this.onRemove.bind(this);
  }

  componentDidMount() {
    this.props.loadEnvironmentData();
  }

  onRemove(environment) {
    this.props.removeEnvironment(environment);
  }

  render() {
    const { loading, error, environments } = this.props;
    const environmentsListProps = {
      loading,
      error,
      environments,
    };
    return (
      <div>
        <Helmet
          title="List environments"
        />
        <Link to="/environments/create" className="pull-right btn btn-primary"><FormattedMessage {...commonMessages.add} /></Link>
        <EnvironmentsList {...environmentsListProps} onRemove={this.onRemove} />
      </div>
    );
  }
}

EnvironmentsListPage.propTypes = {
  loading: PropTypes.bool,
  error: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.bool,
  ]),
  environments: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.array,
  ]),
  loadEnvironmentData: PropTypes.func,
  removeEnvironment: PropTypes.func,
};

const mapStateToProps = createStructuredSelector({
  environments: makeSelectEnvironments(),
  loading: makeSelectLoading(),
  error: makeSelectError(),
});

function mapDispatchToProps(dispatch) {
  return {
    loadEnvironmentData: () => dispatch(loadEnvironmentAction()),
    removeEnvironment: (environment) => dispatch(removeEnvironmentAction(environment)),
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(EnvironmentsListPage);
