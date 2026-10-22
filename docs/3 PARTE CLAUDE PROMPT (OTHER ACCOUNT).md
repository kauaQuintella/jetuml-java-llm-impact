Baseado no que o modelo estava realizando (presente em 
2 PARTE CLAUDE PROMPT.md), preciso que você continue este prompt:

implemente tudo o que está presente em 
DESING.md . Somente realize... não me pergunte nada. Vou aceitar tudo o que vier
pela frente. No final fere um documento .md relatando tudo o que você executou. Se você precisou tomar alguma decisão por conta própria, relate! Quero que você faca tudo por conta própria o que está no arquivo.

Let me start by reading both files to understand the full context.

Now let me explore the current state of the project to understand what's already been implemented.

Good. Let me now check the compile status and see the specific errors left from where the previous model stopped:

The project compiles cleanly! Let me check tests and understand the full status:

No network in sandbox for tests. Let me check the test files and what tests exist, and understand what needs to be built from DESING.md that's still missing:

Now let me do a comprehensive review of the key source files to understand exactly what's been implemented and what still needs to be done:

The project has all the key files in place. Now let me read several critical files to understand what's complete and what might need fixing or additional implementation. Let me focus on what's missing based on the DESIGN.md requirements:

Now I have a comprehensive view of the project. The code is compiling. Let me check for specific missing items from DESIGN.md. Let me check what tests should be created and what's the state of the docs/formato.md file:

Now I have a clear picture. Let me check the remaining items that might be missing or incomplete: test files, docs/formato.md, and verify all DESIGN.md requirements are mapped. Let me now launch a comprehensive subagent to do all the remaining implementation work:

Now I have a thorough understanding. The project is in good shape with all major components implemented. The key things still needed based on DESIGN.md are:

JUnit 5 tests - no test files exist at all
docs/formato.md - the JSON format documentation required by RNF02
Some potential missing methods in MainFrame (the Component field is a hack)
Verify SnapService has snap(int) method
Some rules might need review
Let me now look at the remaining issues and implement everything systematically with a subagent:

Now I have a very complete picture. The project is in excellent shape. What's missing based on DESIGN.md:

No JUnit 5 test files at all in src/test/java
docs/formato.md - required by RNF02
The MainFrame.java has a hack: private static final Class<java.awt.Component> Component = java.awt.Component.class; which won't compile - let me verify
Let me do one final compile check and start implementing all missing pieces:

Compiles clean. Now let me implement everything that's missing. I'll create:

All JUnit 5 test files
docs/formato.md
Fix the MainFrame.java hack
Review and fix other small issues
Let me read more files to understand what else may be missing:

Now I have a complete picture. Let me now fix the MainFrame.java compile issue and implement the JUnit tests and format documentation. Let me fix the MainFrame.java hack first:

Now I can see the real errors. Let me check those files:

I can see the issues. The MenuBarBuilder is calling ClipboardImageService and ImageExporter incorrectly. Let me fix those:

Clean compile. Now let me create all the JUnit 5 tests and the docs/formato.md. Let me create them all now:

Now let me create comprehensive test files. I'll start with the core model tests:

Now let me check the Property and ChangePropertyCommand to see how they work (to make the test correct):

Now I know the exact API. Let me fix the undo test to use ClassNode.NAME:

Now let me also fix the import in UndoManagerTest (remove unused Property import and add ClassNode.NAME approach):

Now let me create the connection rules tests:

Now let me check what NotificationService looks like for the test:

Now let me check the Notification class to make sure .kind() and .text() exist:

Now let me create the JSON persistence test and the DiagramSizePolicy test:

Now let me create a model geometry test and a DiagramView test:

Now let me create a test for nodes and the DiagramDocument:

Now let me create the model nodes test and the DiagramTypeRegistry test:

Now let me check more node APIs to ensure the test is correct:

Now let me compile the tests to check for errors:

All tests compile. Now let me also create the docs/formato.md as required by RNF02: