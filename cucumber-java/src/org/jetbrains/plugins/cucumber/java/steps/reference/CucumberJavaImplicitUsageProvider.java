package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

/**
 * User: Andrey.Vokin
 * Date: 10/4/12
 */
public class CucumberJavaImplicitUsageProvider implements ImplicitUsageProvider {
  @Override
  public boolean isImplicitUsage(PsiElement element) {
    if(element instanceof PsiClass) {
      return CucumberJavaUtil.isStepDefinitionClass((PsiClass)element);
    } else if (element instanceof PsiMethod) {
      return CucumberJavaUtil.isStepDefinition((PsiMethod)element);
    }

    return false;
  }

  @Override
  public boolean isImplicitRead(PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(PsiElement element) {
    return false;
  }
}