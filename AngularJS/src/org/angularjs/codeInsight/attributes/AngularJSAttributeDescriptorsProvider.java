package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.angularjs.codeInsight.attributes.AngularAttributesRegistry.createDescriptor;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      final Map<String, XmlAttributeDescriptor> result = new LinkedHashMap<String, XmlAttributeDescriptor>();
      final Collection<String> docDirectives = AngularIndexUtil.getAllKeys(AngularDirectivesDocIndex.INDEX_ID, project);
      for (String directiveName : docDirectives) {
        if (isApplicable(project, directiveName, xmlTag.getName())) {
          result.put(directiveName, createDescriptor(project, directiveName));
        }
      }
      for (String directiveName : AngularIndexUtil.getAllKeys(AngularDirectivesIndex.INDEX_ID, project)) {
        if (!docDirectives.contains(directiveName)) {
          result.put(directiveName, createDescriptor(project, directiveName));
        }
      }
      return result.values().toArray(new XmlAttributeDescriptor[result.size()]);
    }
    return XmlAttributeDescriptor.EMPTY;
  }

  private static boolean isApplicable(Project project, String directiveName, String tagName) {
    final JSNamedElementProxy directive = AngularIndexUtil.resolve(project, AngularDirectivesDocIndex.INDEX_ID, directiveName);
    final String restrictions = directive != null ? directive.getIndexItem().getTypeString() : null;
    if (restrictions != null) {
      final String[] split = restrictions.split(";", -1);
      final String restrict = split[0];
      final String tag = split[1];
      if (!StringUtil.isEmpty(restrict) && !StringUtil.containsIgnoreCase(restrict, "A")) {
        return false;
      }
      if (!StringUtil.isEmpty(tag) && !StringUtil.equalsIgnoreCase(tag, "ANY") &&
          !StringUtil.equalsIgnoreCase(tag, tagName)) {
        return false;
      }
    }

    return true;
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(final String attrName, XmlTag xmlTag) {
    final String attributeName = normalizeAttributeName(attrName);
    if (xmlTag != null) {
      final Project project = xmlTag.getProject();
      boolean attributeAvailable;
      if (AngularIndexUtil.getAllKeys(AngularDirectivesDocIndex.INDEX_ID, project).contains(attributeName)) {
        attributeAvailable = isApplicable(project, attributeName, xmlTag.getName());
      } else {
        attributeAvailable = AngularIndexUtil.getAllKeys(AngularDirectivesIndex.INDEX_ID, project).contains(attributeName);
      }
      return attributeAvailable ? createDescriptor(project, attributeName) : null;
    }
    return null;
  }

  public static String normalizeAttributeName(String name) {
    if (name == null) return null;
    if (name.startsWith("data-")) {
      name = name.substring(5);
    } else if (name.startsWith("x-")) {
      name = name.substring(2);
    }
    name = name.replace(':', '-');
    name = name.replace('_', '-');
    return name;
  }
}
