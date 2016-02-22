site.reactjs.CallCenterSupervisor.Brs = React.createClass({
    getDefaultProps: function () {
    },

    getInitialState: function () {
        return {
            data: [],
            columns: [],
            page: 1,
            size: 50,
            total: 0,
        };
    },

    componentDidMount: function () {
        var $this = this;
        $this.onHashChange(site.hash);
        site.hash.addHandler($this.onHashChange);
        $this.setState({height: $(window).height() - 180});
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

        var page = $this.state.page;
        var size = $this.state.size;
        var total = $this.state.total;


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

                    <div className="panel panel-default">
                        <div className="panel-body">
                            <BootstrapTable data={_rows} striped={true} hover={true} height={$this.state.height}>
                                <TableHeaderColumn dataField="BR_ID"
                                                   isKey={true}>BR_ID</TableHeaderColumn>
                                <TableHeaderColumn dataField="BR_NAME">BR_NAME</TableHeaderColumn>
                                <TableHeaderColumn dataField="data_count">data_count</TableHeaderColumn>
                                <TableHeaderColumn dataField="call_count">call_count</TableHeaderColumn>
                                <TableHeaderColumn dataField="success_count">success_count</TableHeaderColumn>
                            </BootstrapTable>
                        </div>
                        <div className="panel-footer">
                            <Pagination page={page} size={size} total={total} navLength={12}
                                        onPageRequest={$this.onPageRequest}/>
                        </div>
                    </div>
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
            size: parseInt(params.size) || 50
        }));
    },
    onPageRequest: function (page, size) {
        site.hash.addAll({page: page, size: size});
    },
    getData: function (params) {
        var $this = this;

        console.log("params: " + JSON.stringify(params));

        var eq = [convert(params, {
            'BRS.DISTRIBUTION_HOUSE_ID': function (val) {
                return parseInt(val);
            }
        })];

        eq = removeEmptyChilds(eq);

        eq = eq.map(function (v) {
            for (var x in v) {
                return {'name': x, 'value': v[x]};
            }
        })

        console.log("eq: " + JSON.stringify(eq));

        eb.send('BR_WISE_CALL_SUMMARY', merge2(params, {eq: eq}), {}, function (err, msg) {
            if (!!err) console.error(err);
            msg.body = msg.body || {};
            msg.body.data = msg.body.data || [];

            var obj = msg.body.data[0];
            var columns = [];
            for (var x in obj) {
                columns.push({key: x, name: x});
            }
            console.log(columns)
            var data = msg.body.data;
            var pg = msg.body.pagination || {page: 1, size: 50, total: 0};
            $this.setState({
                columns: columns,
                data: data,
                page: pg.page,
                size: pg.size,
                total: pg.total
            });
        });
    }
});