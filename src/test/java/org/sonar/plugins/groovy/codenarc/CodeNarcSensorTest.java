/*
 * Sonar Groovy Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.groovy.codenarc;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.groovy.GroovyPlugin;
import org.sonar.plugins.groovy.foundation.Groovy;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CodeNarcSensorTest {

  private RuleFinder ruleFinder;
  private RulesProfile profile;
  private CodeNarcSensor sensor;
  private Settings settings;
  private ResourcePerspectives perspectives;
  private ModuleFileSystem moduleFileSystem;
  private DefaultFileSystem fileSystem;
  private Project project;
  private SensorContext context;
  private Issuable issuable;

  @org.junit.Rule
  public TemporaryFolder projectdir = new TemporaryFolder();

  @Before
  public void setUp() {
    ruleFinder = mock(RuleFinder.class);
    profile = mock(RulesProfile.class);
    settings = mock(Settings.class);
    perspectives = mock(ResourcePerspectives.class);
    moduleFileSystem = mock(ModuleFileSystem.class);
    project = mock(Project.class);
    context = mock(SensorContext.class);
    fileSystem = new DefaultFileSystem();
    sensor = new CodeNarcSensor(settings, perspectives, moduleFileSystem, fileSystem, profile, ruleFinder);

    issuable = mock(Issuable.class);
    IssueBuilder issueBuilder = mock(IssueBuilder.class);
    when(issuable.newIssueBuilder()).thenReturn(issueBuilder);
    when(issueBuilder.message(anyString())).thenReturn(issueBuilder);
    when(issueBuilder.line(anyInt())).thenReturn(issueBuilder);
    when(issueBuilder.ruleKey(any(RuleKey.class))).thenReturn(issueBuilder);
    when(issueBuilder.build()).thenReturn(mock(Issue.class));
    when(perspectives.as(any(Class.class), any(InputFile.class))).thenReturn(issuable);
  }

  @Test
  public void should_execute_on_project() {
    fileSystem.add(new DefaultInputFile("fake.groovy").setLanguage(Groovy.KEY));
    when(profile.getActiveRulesByRepository(CodeNarcRulesDefinition.REPOSITORY_KEY))
      .thenReturn(Arrays.asList(new ActiveRule()));
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void should_not_execute_when_no_active_rules() {
    fileSystem.add(new DefaultInputFile("fake.groovy").setLanguage(Groovy.KEY));
    when(profile.getActiveRulesByRepository(CodeNarcRulesDefinition.REPOSITORY_KEY))
      .thenReturn(Collections.EMPTY_LIST);
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void should_not_execute_if_no_groovy_files() {
    when(profile.getActiveRulesByRepository(CodeNarcRulesDefinition.REPOSITORY_KEY))
      .thenReturn(Arrays.asList(new ActiveRule()));
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void should_parse() {
    Rule rule = Rule.create();
    rule.setRepositoryKey("repoKey");
    rule.setKey("ruleKey");
    when(ruleFinder.find(any(RuleQuery.class))).thenReturn(rule);

    FileSystem fileSystem = mock(FileSystem.class);
    when(fileSystem.predicates()).thenReturn(mock(FilePredicates.class));
    when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(mock(InputFile.class));

    File report = FileUtils.toFile(getClass().getResource("parsing/sample.xml"));
    when(settings.getString(GroovyPlugin.CODENARC_REPORT_PATH)).thenReturn(report.getAbsolutePath());

    sensor = new CodeNarcSensor(settings, perspectives, moduleFileSystem, fileSystem, profile, ruleFinder);
    sensor.analyse(project, context);

    verify(issuable, atLeastOnce()).addIssue(any(Issue.class));
  }

  @Test
  public void should_parse_but_not_add_issue_if_rule_not_found() {
    when(ruleFinder.find(any(RuleQuery.class))).thenReturn(null);

    File report = FileUtils.toFile(getClass().getResource("parsing/sample.xml"));
    when(settings.getString(GroovyPlugin.CODENARC_REPORT_PATH)).thenReturn(report.getAbsolutePath());

    sensor.analyse(project, context);

    verify(issuable, never()).addIssue(any(Issue.class));
  }

  @Test
  public void should_parse_but_not_add_issue_if_inputFile_not_found() {
    when(ruleFinder.find(any(RuleQuery.class))).thenReturn(Rule.create());

    File report = FileUtils.toFile(getClass().getResource("parsing/sample.xml"));
    when(settings.getString(GroovyPlugin.CODENARC_REPORT_PATH)).thenReturn(report.getAbsolutePath());

    sensor.analyse(project, context);

    verify(issuable, never()).addIssue(any(Issue.class));
  }

  @Test
  public void should_parse_but_not_add_issue_if_issuable_not_found() {
    when(ruleFinder.find(any(RuleQuery.class))).thenReturn(Rule.create());

    FileSystem fileSystem = mock(FileSystem.class);
    when(fileSystem.predicates()).thenReturn(mock(FilePredicates.class));
    when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(mock(InputFile.class));

    File report = FileUtils.toFile(getClass().getResource("parsing/sample.xml"));
    when(settings.getString(GroovyPlugin.CODENARC_REPORT_PATH)).thenReturn(report.getAbsolutePath());

    when(perspectives.as(any(Class.class), any(InputFile.class))).thenReturn(null);

    sensor = new CodeNarcSensor(settings, perspectives, moduleFileSystem, fileSystem, profile, ruleFinder);
    sensor.analyse(project, context);

    verify(issuable, never()).addIssue(any(Issue.class));
  }

  @Test
  public void should_run_code_narc() throws IOException {
    File sonarhome = projectdir.newFolder("sonarhome");
    PrintWriter pw = new PrintWriter(new File(sonarhome, "sample.groovy"));
    pw.write("package source\nclass SourceFile1 {\n}");
    pw.close();

    Rule rule = Rule.create();
    rule.setRepositoryKey("repoKey");
    rule.setKey("ruleKey");
    when(ruleFinder.find(any(RuleQuery.class))).thenReturn(rule);

    FileSystem fileSystem = mock(FileSystem.class);
    when(fileSystem.predicates()).thenReturn(mock(FilePredicates.class));
    when(fileSystem.inputFile(any(FilePredicate.class))).thenReturn(mock(InputFile.class));
    when(fileSystem.workDir()).thenReturn(sonarhome);

    ActiveRule activeRule = mock(ActiveRule.class);
    when(activeRule.getRuleKey()).thenReturn("org.codenarc.rule.basic.EmptyClassRule");
    when(profile.getActiveRulesByRepository(CodeNarcRulesDefinition.REPOSITORY_KEY)).thenReturn(Arrays.asList(activeRule));
    when(settings.getString(GroovyPlugin.CODENARC_REPORT_PATH)).thenReturn("");
    when(moduleFileSystem.sourceDirs()).thenReturn(Lists.newArrayList(sonarhome));

    sensor = new CodeNarcSensor(settings, perspectives, moduleFileSystem, fileSystem, profile, ruleFinder);
    sensor.analyse(project, context);

    verify(issuable, atLeastOnce()).addIssue(any(Issue.class));
  }

  @Test
  public void test_toString() {
    assertThat(sensor.toString()).isEqualTo("CodeNarc");
  }

}
