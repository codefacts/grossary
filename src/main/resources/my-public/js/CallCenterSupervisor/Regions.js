site.reactjs.CallCenterSupervisor.Regions = React.createClass({
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
        $this.getData();
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
                        <TableHeaderColumn dataField="REGION_ID" isKey={true}>REGION_ID</TableHeaderColumn>
                        <TableHeaderColumn dataField="REGION_NAME"
                                           dataFormat={function (cell, row) {
                                                return <a href={'#/areas?AREAS.REGION_ID=' + row['REGION_ID']}>{cell}</a>;
                                           }}>REGION_NAME</TableHeaderColumn>
                        <TableHeaderColumn dataField="data_count">data_count</TableHeaderColumn>
                        <TableHeaderColumn dataField="call_count">call_count</TableHeaderColumn>
                        <TableHeaderColumn dataField="success_count">success_count</TableHeaderColumn>
                    </BootstrapTable>

                </div>
            </div>
        );
    },
    getData: function (params) {
        var $this = this;
        eb.send('REGION_WISE_CALL_SUMMARY', {}, {}, function (err, msg) {
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
})
;