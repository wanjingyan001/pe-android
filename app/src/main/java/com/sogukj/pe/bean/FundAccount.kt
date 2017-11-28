package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/11/24.
 */
class FundAccount : Serializable {
    var simpleName: String = ""//基金名称
    var contributeSize: String = "0"//基金认缴规模
    var actualSize: String = "0"//基金实缴规模
    var investedNum: Int = 0//已投项目数量
    var investedMoney: String = ""//已投金额基金余额
    var withdraw: String = ""//已收回金额
    var balance: String = ""//基金余额
    var quitNum: Int = 0//退出项目数量(部分)
    var quitAll: Int = 0//退出项目数量(全部)
    var quitCost: String = ""//已退出成本
    var quitIncome: String = ""//退出项目收益
    var investmentAmount: String = ""//pe项目投资金额
    var profitLoss: String = ""//pe已上市项目浮盈/亏
    var fixProfit: String = ""//定增项目浮盈/亏
    var valuations: String = ""//估值
    var sponsor: String = ""//海通出资
    var freeFunds: String = "0"//自由资金引入
    var RaiseFunds: String = "0"//外部筹集资金
    var fundSize: String = ""//基金规模
}