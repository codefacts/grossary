site.reactjs.CallCenterSupervisor.Areas = React.createClass({
    getDefaultProps: function () {
    },

    getInitialState: function () {
        return {
            data: [],
            columns: [],
        };
    },

    componentDidMount: function () {
        var $this = this;
        $this.onHashChange(site.hash)
        site.hash.addHandler($this.onHashChange);
        $this.setState({height: $(window).height() - 80});
    },

    componentWillUnmount: function () {
        var $this = this;
        site.hash.removeHandler($this.onHashChange);
    },

    render: function () {
        var $this = this;
        var _rows = $this.state.data;

        var rowGetter = function (i) {
            return _rows[i];
        };

        var columns = $this.state.columns;

        var totals = _rows.reduce(function (p, n) {
            return {
                data_count: p.data_count + n.data_count,
                call_count: p.call_count + n.call_count,
                success_count: p.success_count + n.success_count
            }
        }, {data_count: 0, call_count: 0, success_count: 0, AREA_ID: 84335241584, AREA_NAME: 'Total'});

        if (_rows.length > 0) _rows.push(totals);

        return (
            <div className="row">

                <div className="col-md-12">

                    <BootstrapTable data={_rows} striped={true} hover={true}>
                        <TableHeaderColumn dataField="AREA_ID" isKey={true}>AREA_ID</TableHeaderColumn>
                        <TableHeaderColumn dataField="AREA_NAME"
                                           dataFormat={function (cell, row) {
                                                return <a href={'#/distribution-houses?DISTRIBUTION_HOUSES.AREA_ID=' + row['AREA_ID']}>{cell}</a>;
                                           }}>AREA_NAME</TableHeaderColumn>
                        <TableHeaderColumn dataField="data_count">data_count</TableHeaderColumn>
                        <TableHeaderColumn dataField="call_count">call_count</TableHeaderColumn>
                        <TableHeaderColumn dataField="success_count">success_count</TableHeaderColumn>
                    </BootstrapTable>

                </div>
            </div>
        );
    },
    onHashChange: function (hash) {
        var $this = this;
        var params = hash.params();
        console.log("params: " + JSON.stringify(params))
        $this.getData(merge2(params, {
            page: parseInt(params.page) || 1,
            size: parseInt(params.size) || 200
        }));
    },
    getData: function (params) {
        var $this = this;

        console.log("params: " + JSON.stringify(params));

        var eq = [convert(params, {
            'AREAS.REGION_ID': function (val) {
                return parseInt(val);
            }
        })];

        eq = removeEmptyChilds(eq);

        eq = eq.map(function (v) {
            for (var x in v) {
                return {'name': x, 'value': v[x]};
            }
        })

        eb.send('AREA_WISE_CALL_SUMMARY', {
            eq: eq
        }, {}, function (err, msg) {
            if (!!err) console.error(err);
            var obj = msg.body.data[0] || {};
            var columns = [];
            for (var x in obj) {
                columns.push({key: x, name: x});
            }
            console.log(columns)
            $this.setState({
                columns: columns,
                data: msg.body.data
            });
        });
    }
});