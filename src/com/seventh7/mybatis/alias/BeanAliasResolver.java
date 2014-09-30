package com.seventh7.mybatis.alias;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.spring.CommonSpringModel;
import com.intellij.spring.SpringManager;
import com.intellij.spring.model.SpringBeanPointer;
import com.intellij.spring.model.utils.SpringPropertyUtils;
import com.intellij.spring.model.xml.beans.SpringPropertyDefinition;
import com.seventh7.mybatis.util.JavaUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author yanglin
 */
public class BeanAliasResolver extends PackageAliasResolver {

  private static final String MAPPER_ALIAS_PACKAGE_CLASS = "org.mybatis.spring.SqlSessionFactoryBean";
  private static final String MAPPER_ALIAS_PROPERTY = "typeAliasesPackage";
  private ModuleManager moduleManager;
  private SpringManager springManager;
  private Project project;

  public BeanAliasResolver(Project project) {
    super(project);
    this.moduleManager = ModuleManager.getInstance(project);
    this.springManager = SpringManager.getInstance(project);
    this.project = project;
  }

  @NotNull
  @Override
  public Collection<String> getPackages(@Nullable PsiElement element) {
    Set<String> res = Sets.newHashSet();
    for (Module module : moduleManager.getModules()) {
      for (CommonSpringModel springModel : springManager.getCombinedModel(module).getModelsToProcess()) {
        addPackages(res, springModel);
      }
    }
    return res;
  }

  private void addPackages(Set<String> res, CommonSpringModel springModel) {
    Optional<PsiClass> aliasPackageClazz = JavaUtils.findClazz(project, MAPPER_ALIAS_PACKAGE_CLASS);
    if (!aliasPackageClazz.isPresent()) {
      return;
    }
    Collection<SpringBeanPointer> domBeans = springModel.getAllDomBeans();
    for (SpringBeanPointer pointer : domBeans) {
      PsiClass beanClass = pointer.getBeanClass();
      if (beanClass == null || !beanClass.equals(aliasPackageClazz)) {
        continue;
      }
      SpringPropertyDefinition basePackages = SpringPropertyUtils.findPropertyByName(pointer.getSpringBean(),
                                                                                     MAPPER_ALIAS_PROPERTY);
      if (basePackages != null) {
        final String value = basePackages.getValueElement().getStringValue();
        if (value != null) {
          Collections.addAll(res, value.split(",|;"));
        }
      }
    }
  }

}
