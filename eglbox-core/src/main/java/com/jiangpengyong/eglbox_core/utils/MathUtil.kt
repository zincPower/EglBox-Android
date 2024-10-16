import kotlin.math.round

fun roundToEven(num: Float): Int {
    // 四舍五入到最近的整数
    var rounded = round(num).toInt()
    // 检查是否为偶数，如果不是，则调整
    if (rounded % 2 != 0) {
        // 检查是增加还是减少 1 会更接近原始数值
        if ((num - (rounded - 1)) < ((rounded + 1) - num)) {
            rounded -= 1  // 减少 1
        } else {
            rounded += 1  // 增加 1
        }
    }
    return rounded
}