site.reactjs.SummaryDetailsTable = React.createClass({
    getDefaultProps: function () {
        return {
            data: [],
            onInit: function () {
            }
        };
    },
    getInitialState: function () {
        return {
            bodyHeight: this.bodyHeight(),
            __render: true
        };
    },
    componentDidMount: function () {
        var $this = this;
        $this.props.onInit($this);
        $(window).bind("resize", $this.onWindowResize);
    },
    componentWillUnmount: function () {
        var $this = this;
        $(window).unbind("resize", $this.onWindowResize);
    },
    onWindowResize: function () {
        var $this = this;
        $this.setState({bodyHeight: $this.bodyHeight(), __render: !$this.state.__render});
    },
    render: function () {
        var $this = this;
        var data = $this.props.data;

        var width = $(window).width() - 20;

        width = width / 5;

        var cols = [
            {width: width + 'px', label: 'Grocery', name: 'grocery'},
            {width: width + 'px', label: 'Location', name: 'location'},
            {width: width + 'px', label: 'Poin', name: 'posNo'},
            {width: width + 'px', label: 'Date', name: 'date', apply: $this.formatDate},
            {width: width + 'px', label: 'Total', name: 'totalCount'},
        ].map(function (col) {
                col.apply = !col.apply ? function (s) {
                    return s
                } : col.apply;
                return col;
            });

        return (
            <div className="table-responsive TablePrimary"
                 style={{border: '1px solid #ddd', height: $this.state.bodyHeight + 'px'}}>

                <table className="table table-stripped table-bordered table-hover MainTable"
                       style={{marginTop: '37px', height: $this.tableHeight($this.state.bodyHeight) + 'px',
                       display:'block', border: 0, borderTop: '1px solid #ddd'}}>

                    <thead style={{display: 'block', position: 'absolute', top: '0', left: '16px'}}>

                    <tr>
                        {cols.map(function (col) {
                            return (<th key={col.name}
                                        style={{minWidth: col.width, maxWidth: col.width, borderBottom: 0}}>{col.label}</th>);
                        })}
                    </tr>
                    </thead>
                    <tbody className="MainTableBody">
                    {(function () {
                        var i = 0;
                        return (
                            data.map(function (v) {
                                return (
                                    <tr key={Math.random()}>
                                        {cols.map(function (col) {
                                            return (<td key={col.name}
                                                        style={{minWidth: col.width, maxWidth: col.width, borderBottom: 0}}>{col.apply(v[col.name])}</td>);
                                        })}
                                    </tr>
                                );
                            })
                        );
                    })()}
                    </tbody>
                </table>
            </div>
        );
    },
    formatDate: function (date) {
        return formatDate(new Date(date));
    },
    tableHeight: function (height) {
        return height - 60;
    },
    bodyHeight: function () {
        return $(window).height() - 100;
    }
});