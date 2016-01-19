site.reactjs.Step2 = React.createClass({
    getDefaultProps: function () {
        var $this = this;
        return {
            onInit: function () {
            }
        }
    },
    getInitialState: function () {
        var $this = this;
        return {
            primaryTableRef: null,
            data: []
        };
    },
    componentDidMount: function () {
        var $this = this;
        $this.props.onInit($this);
        $this.updateData();
    },
    render: function () {
        var $this = this;
        return (
            <div className="row">

                <div id="container" className="col-md-12">

                    <site.reactjs.SummaryTable onInit={$this.onPrimaryTableInit} data={$this.state.data}/>

                </div>

            </div>
        );
    },

    onPrimaryTableInit: function (primaryTableRef) {
        this.setState({primaryTableRef: primaryTableRef});
    },

    updateData: function () {
        var $this = this;
        $.ajax({
            url: '/contacts/groupByCount',
            cache: false,
            success: function (data) {
                $this.setState({data: data});
            },
            error: function () {
                alert("Error in Server. Error Code: INFO");
            }
        });
    }
});