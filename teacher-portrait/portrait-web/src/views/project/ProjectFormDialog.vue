<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑纵向项目' : '新增纵向项目'"
    :width="isMobile ? '90%' : '650px'"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
    @closed="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" :label-width="isMobile ? '100%' : '100px'" :label-position="isMobile ? 'top' : 'right'">
      <el-row :gutter="isMobile ? 0 : 20">
        <el-col :span="isMobile ? 24 : 12">
          <el-form-item label="项目名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入项目名称" :style="isMobile ? 'width:100%' : ''" />
          </el-form-item>
        </el-col>
        <el-col :span="isMobile ? 24 : 12">
          <el-form-item label="项目编号" prop="projectNo">
            <el-input v-model="form.projectNo" placeholder="请输入项目编号" :style="isMobile ? 'width:100%' : ''" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="isMobile ? 0 : 20">
        <el-col :span="isMobile ? 24 : 12">
          <el-form-item label="项目级别" prop="level">
            <el-select v-model="form.level" placeholder="请选择项目级别" style="width:100%">
              <el-option label="国家级" value="国家级" />
              <el-option label="省部级" value="省部级" />
              <el-option label="市厅级" value="市厅级" />
              <el-option label="校级" value="校级" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="isMobile ? 24 : 12">
          <el-form-item label="参与角色" prop="role">
            <el-select v-model="form.role" placeholder="请选择参与角色" style="width:100%">
              <el-option label="主持" value="主持" />
              <el-option label="参与" value="参与" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="isMobile ? 0 : 20">
        <el-col :span="isMobile ? 24 : 12">
          <el-form-item label="项目状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择项目状态" style="width:100%">
              <el-option label="在研" value="在研" />
              <el-option label="已结题" value="已结题" />
              <el-option label="延期" value="延期" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="isMobile ? 24 : 12">
          <el-form-item label="来源单位" prop="sourceUnit">
            <el-input v-model="form.sourceUnit" placeholder="请输入来源单位" :style="isMobile ? 'width:100%' : ''" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="isMobile ? 0 : 20">
        <el-col :span="isMobile ? 24 : 12">
          <el-form-item label="立项时间" prop="startDate">
            <el-date-picker v-model="form.startDate" type="date" placeholder="请选择立项时间" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="isMobile ? 24 : 12">
          <el-form-item label="计划完成时间">
            <el-date-picker v-model="form.plannedEndDate" type="date" placeholder="请选择计划完成时间" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="isMobile ? 0 : 20">
        <el-col :span="isMobile ? 24 : 12">
          <el-form-item label="项目经费(万元)">
            <el-input-number v-model="form.funding" :min="0" :precision="2" :step="1" placeholder="请输入经费" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" :style="isMobile ? 'width:100%' : ''" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div :class="isMobile ? 'mobile-dialog-footer' : ''">
        <el-button @click="$emit('update:visible', false)">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { createVerticalProject, updateVerticalProject } from '../../api/verticalProject'
import { useResponsive } from '../../composables/useResponsive'

// 响应式布局检测
const responsive = useResponsive()
const isMobile = computed(() => responsive.isMobile.value)

const props = defineProps({
  visible: Boolean,
  editData: Object
})

const emit = defineEmits(['update:visible', 'success'])

const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  projectNo: '',
  level: '',
  sourceUnit: '',
  startDate: '',
  plannedEndDate: '',
  funding: null,
  role: '',
  status: '',
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择项目级别', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择立项时间', trigger: 'change' }],
  role: [{ required: true, message: '请选择参与角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择项目状态', trigger: 'change' }]
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.editData) {
      isEdit.value = true
      Object.keys(form).forEach(key => {
        form[key] = props.editData[key] !== undefined ? props.editData[key] : ''
      })
    } else {
      isEdit.value = false
      resetForm()
    }
  }
})

function resetForm() {
  Object.keys(form).forEach(key => {
    form[key] = key === 'funding' ? null : ''
  })
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateVerticalProject(props.editData.id, form)
      ElMessage.success('编辑成功')
    } else {
      await createVerticalProject(form)
      ElMessage.success('新增成功')
    }
    emit('success')
    emit('update:visible', false)
  } catch (error) {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  formRef.value?.resetFields()
  resetForm()
}
</script>

<style scoped>
/* 移动端底部按钮样式 */
.mobile-dialog-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.mobile-dialog-footer :deep(.el-button) {
  flex: 1;
  margin: 0;
}
</style>
