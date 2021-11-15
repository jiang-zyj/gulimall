package com.zyj.common.valid;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @program: gulimall
 * @ClassName ListValueConstraintValidator
 * @author: YaJun
 * @Date: 2021 - 11 - 11 - 21:35
 * @Package: com.zyj.common.valid
 * @Description: 校验器；使用自定义注解 @ListValue 校验 Integer 类型的值
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private final Set<Integer> set = new HashSet<>();

    /**
     * 初始化方法
     * @param constraintAnnotation
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        // 如果数组不为空，则将数据放入 set 中
        if (vals.length != 0) {
            for (int val : vals) {
                set.add(val);
            }
        }
    }

    /**
     * 判断是否校验成功
     * @param value 需要校验的值
     * @param context
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // 判断校验的值是否包含在字段自定义时填充的值
        return set.contains(value);
    }
}
