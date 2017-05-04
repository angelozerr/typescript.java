class Logo extends React.Component {
        render(){
            return <img className="center" {...this.props} />;
        }
      }

      Logo.propTypes = {
          src: React.PropTypes.string.isRequired
      };

      class Label extends React.Component {
        render() {
            return <p className="default-label" {...this.props}> Hello World <span className="name"> {
                this.props.name
            } </span>
            </p>;
        }
      }

      class Input extends React.Component {
        render() {
            return <input
                className="default-input"
                placeholder="Enter your name"
                {...this.props}
                type="text"
             />;
        }
      }

      class HelloWidget extends React.Component{
        constructor(props) {
          super(props);
          this.handleChange = this.handleChange.bind(this)
          this.state = {
                name: ''
          };
        }

        handleChange(e) {
          this.setState({
            name: e.target.value
          });
        }

        render() {
            return <div className="widget">
                <Logo className="center" src="http://goo.gl/fx5Zwn"/>
                <Label className="default-label" name={this.state.name} />
                <Input className="default-input" onChange={this.handleChange} /> </div>;
        }
      }

      React.render(<HelloWidget />, document.getElementById('container'));