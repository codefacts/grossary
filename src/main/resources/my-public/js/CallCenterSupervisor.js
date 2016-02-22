site.reactjs.CallCenterSupervisor.App = React.createClass({
    getInitialState: function () {
        return {
            path: '',
            params: {}
        };
    },
    componentDidMount: function () {
        var $this = this;

        $this.setState({
            path: site.hash.path(),
            params: site.hash.params()
        });

        $this.router();
    },

    render: function () {
        var $this = this;
        var path = $this.state.path;
        var params = $this.state.params;
        return (
            <div className="container">

                <site.reactjs.CallCenterSupervisor.Navbar/>

                {function () {
                    var map = {
                        '/': function (params) {
                            console.log("/regions");
                            return (
                                <site.reactjs.CallCenterSupervisor.Regions />
                            );
                        },
                        '/areas': function (params) {
                            console.log("/areas");
                            return (
                                <site.reactjs.CallCenterSupervisor.Areas />
                            );
                        },
                        '/distribution-houses': function (params) {
                            console.log("/distribution-houses");
                            return (
                                <site.reactjs.CallCenterSupervisor.DistributionHouses/>
                            );
                        },
                        '/brs': function (params) {
                            console.log("/brs");
                            return (
                                <site.reactjs.CallCenterSupervisor.Brs />
                            );
                        }
                    };

                    return (map[path] || function () {
                    })(params);
                }()}

            </div>
        );
    },
    router: function () {
        var $this = this;
        site.hash.addHandler(function (hash) {
            $this.setState({
                path: hash.path(),
                params: hash.params()
            });
        });
    },
});