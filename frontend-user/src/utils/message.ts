import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 成功提示
 */
export const showSuccess = (message: string) => {
    ElMessage.success({
        message,
        customClass: 'phms-toast',
        duration: 2000,
        showClose: true
    })
}

/**
 * 错误提示
 */
export const showError = (message: string) => {
    ElMessage.error({
        message,
        customClass: 'phms-toast',
        duration: 3000,
        showClose: true
    })
}

/**
 * 警告提示
 */
export const showWarning = (message: string) => {
    ElMessage.warning({
        message,
        customClass: 'phms-toast',
        duration: 2500,
        showClose: true
    })
}

/**
 * 信息提示
 */
export const showInfo = (message: string) => {
    ElMessage.info({
        message,
        customClass: 'phms-toast',
        duration: 2000,
        showClose: true
    })
}

/**
 * 确认对话框
 */
export const showConfirm = async (message: string, title = '提示'): Promise<boolean> => {
    try {
        await ElMessageBox.confirm(message, title, {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        return true
    } catch {
        return false
    }
}
