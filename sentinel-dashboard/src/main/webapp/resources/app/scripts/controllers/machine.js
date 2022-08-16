var app = angular.module('sentinelDashboardApp');

/*
 *  排序规则
*/
function jsonSort(array) {
    let res = [];
    let isTrue = [];
    let isFalse = [];
    //健康状态为True的放在前面
    array.forEach((item) => {
        if (item['healthy'] === true) {
            isTrue.push(item);
        } else {
            isFalse.push(item);
        }
    });


    //健康机器名里面有 -xop 的放在前面
    let haveXop = [];
    let noneXop = [];
    isTrue.forEach((item) => {

        if (item['hostname'].indexOf("-xop") !== -1) {
            haveXop.push(item);
        } else {
            noneXop.push(item);
        }
    });
    //组合
    isTrue = [...haveXop, ...noneXop];
    //组合
    res = [...isTrue, ...isFalse];
    return res;
}


app.controller('MachineCtl', ['$scope', '$stateParams', 'MachineService',
    function ($scope, $stateParams, MachineService) {


        $scope.app = $stateParams.app;
        $scope.propertyName = '';
        $scope.reverse = false;
        $scope.currentPage = 1;
        $scope.machines = [];

        $scope.machinesPageConfig = {
            pageSize: 10,
            currentPageIndex: 1,
            totalPage: 1,
            totalCount: 0,
        };

        $scope.sortBy = function (propertyName) {
            // console.log('machine sortBy ' + propertyName);
            $scope.reverse = ($scope.propertyName === propertyName) ? !$scope.reverse : false;
            $scope.propertyName = propertyName;
        };

        $scope.reloadMachines = function () {
            MachineService.getAppMachines($scope.app).success(
                function (data) {
                    // console.log('get machines: ' + data.data[0].hostname)
                    if (data.code == 0 && data.data) {
                        $scope.machines = jsonSort(data.data);

                        var healthy = 0;
                        $scope.machines.forEach(function (item) {
                            if (item.healthy) {
                                healthy++;
                            }
                            if (!item.hostname) {
                                item.hostname = '未知';
                            }
                        })
                        $scope.healthyCount = healthy;
                        $scope.machinesPageConfig.totalCount = $scope.machines.length;
                    } else {
                        $scope.machines = [];
                        $scope.healthyCount = 0;
                    }
                }
            );
        };

        $scope.removeMachine = function (ip, port) {
            if (!confirm("confirm to remove machine [" + ip + ":" + port + "]?")) {
                return;
            }
            MachineService.removeAppMachine($scope.app, ip, port).success(
                function (data) {
                    if (data.code == 0) {
                        $scope.reloadMachines();
                    } else {
                        alert("remove failed");
                    }
                }
            );
        };

        $scope.reloadMachines();

    }]);
