package org.angularjs.codeInsight;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.resolve.ImplicitJSVariableImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.Consumer;
import org.angularjs.lang.psi.AngularJSAsExpression;
import org.angularjs.lang.psi.AngularJSRecursiveVisitor;
import org.angularjs.lang.psi.AngularJSRepeatExpression;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSProcessor {
  private static final Map<String, String> NG_REPEAT_IMPLICITS = new HashMap<String, String>();
  static {
    NG_REPEAT_IMPLICITS.put("$index", "Number");
    NG_REPEAT_IMPLICITS.put("$first", "Boolean");
    NG_REPEAT_IMPLICITS.put("$middle", "Boolean");
    NG_REPEAT_IMPLICITS.put("$last", "Boolean");
    NG_REPEAT_IMPLICITS.put("$even", "Boolean");
    NG_REPEAT_IMPLICITS.put("$odd", "Boolean");
  }

  public static void process(final PsiElement element, final Consumer<JSNamedElement> consumer) {
    final PsiFile hostFile = FileContextUtil.getContextFile(element);
    if (hostFile == null) return;

    final XmlFile file = (XmlFile)hostFile;
    final JSResolveUtil.JSInjectedFilesVisitor visitor = new JSResolveUtil.JSInjectedFilesVisitor() {
      @Override
      protected void process(JSFile file) {
        file.accept(new AngularJSRecursiveVisitor() {
          @Override
          public void visitJSVariable(JSVariable node) {
            if (scopeMatches(element, node)) {
              consumer.consume(node);
            }
            super.visitJSVariable(node);
          }

          @Override
          public void visitAngularJSAsExpression(AngularJSAsExpression asExpression) {
            final JSDefinitionExpression def = asExpression.getDefinition();
            if (def != null && scopeMatches(element, asExpression)) {
              consumer.consume(def);
            }
          }

          @Override
          public void visitAngularJSRepeatExpression(AngularJSRepeatExpression repeatExpression) {
            if (scopeMatches(element, repeatExpression)) {
              for (JSDefinitionExpression def : repeatExpression.getDefinitions()) {
                consumer.consume(def);
              }
              for (Map.Entry<String, String> entry : NG_REPEAT_IMPLICITS.entrySet()) {
                consumer.consume(new ImplicitJSVariableImpl(entry.getKey(), entry.getValue(), repeatExpression));
              }
            }
            super.visitAngularJSRepeatExpression(repeatExpression);
          }
        });
      }
    };
    final XmlDocument document = file.getDocument();
    if (document == null) return;
    for (XmlTag tag : PsiTreeUtil.getChildrenOfTypeAsList(document, XmlTag.class)) {
      new XmlBackedJSClassImpl.InjectedScriptsVisitor(tag, null, true, true, visitor, true).go();
    }
  }

  private static boolean scopeMatches(PsiElement element, PsiElement declaration) {
    final InjectedLanguageManager injector = InjectedLanguageManager.getInstance(element.getProject());
    final XmlTagChild elementContainer = PsiTreeUtil.getNonStrictParentOfType(injector.getInjectionHost(element),
                                                                              XmlTag.class, XmlText.class);
    final XmlTagChild declarationContainer = PsiTreeUtil.getNonStrictParentOfType(injector.getInjectionHost(declaration),
                                                                                  XmlTag.class, XmlText.class);
    if (elementContainer != null && declarationContainer != null) {
      return PsiTreeUtil.isAncestor(declarationContainer, elementContainer, true);
    }
    return true;
  }
}
