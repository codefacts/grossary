site.reactjs.WorkDayDetailsTable = React.createClass({
    getDefaultProps: function () {
        return {
            data: [],
            onInit: function () {
            }
        };
    },
    getInitialState: function () {
        return this.interceptState({
            bodyHeight: this.bodyHeight(),
            data: this.props.data,
            __render: true
        });
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
    shouldComponentUpdate: function (nextProps, nextState) {
        return this.state.__render !== nextState.__render;
    },
    render: function () {
        var $this = this;
        var data = $this.state.data;

        var cols = [
            {
                width: '100px', label: 'Signatue', name: 'signature', apply: function (name) {
                return (<img style={{width: '90px'}} src={'/app_data/images/' + name}/>)
            }
            },
            {width: '100px', label: 'ID', name: 'id'},
            {width: '150px', label: 'Name', name: 'name'},
            {width: '100px', label: 'Phone', name: 'phone'},
            {width: '100px', label: 'Age Group', name: 'ageGroup'},
            {width: '200px', label: 'Grocery', name: 'grocery'},

            {width: '200px', label: 'Location', name: 'location'},
            {width: '100px', label: 'Pos No', name: 'posNo'},
            {width: '150px', label: 'Address', name: 'address'},
            {width: '100px', label: 'Date', name: 'date', apply: function (date) {
                return formatDate(new Date(date));
            }},
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
                        return (
                            data.map(function (v) {
                                return (
                                    <tr key={v.id}>
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

    tableHeight: function (height) {
        return height - 60;
    },
    bodyHeight: function () {
        return $(window).height() - 52;
    },
    updateData: function (data) {
        var $this = this;
        $this.setState(this.interceptState({data: data, __render: !$this.state.__render}));
    },
    interceptState: function (state) {
        state.data.forEach(function (v, i) {
            v.index = i;
        });
        return state;
    }
});