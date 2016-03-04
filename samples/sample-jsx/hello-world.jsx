import * as React from 'react';

class HelloWorld extends React.Component {
    render() {
        return <h1>Hello from {this.props.phrase}!</h1>;
    }
}

var a = React.createClass();

export default HelloWorld;