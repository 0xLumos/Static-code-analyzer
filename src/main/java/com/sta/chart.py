import matplotlib.pyplot as plt

# Data
warning_types = [
    "UseExplicitTypes", "MethodArgumentCouldBeFinal", "UnnecessaryModifier",
    "ShortClassName", "AtLeastOneConstructor", "ConfusingTernary",
    "ShortMethodName", "UselessParentheses", "OnlyOneReturn",
    "ControlStatementBraces", "FieldDeclarationsShouldBeAtStartOfClass",
    "EmptyMethodInAbstractClassShouldBeAbstract", "LinguisticNaming",
    "UnnecessaryConstructor", "BooleanGetMethodName", "ShortVariable",
    "UnnecessaryImport", "LocalVariableNamingConventions",
    "TooManyStaticImports", "LongVariable", "UnnecessaryFullyQualifiedName",
    "CallSuperInConstructor", "EmptyControlStatement", "LocalVariableCouldBeFinal",
    "UnnecessaryCast", "UnnecessaryAnnotationValueElement", "MethodNamingConventions",
    "FieldNamingConventions", "FinalParameterInAbstractMethod",
    "CommentDefaultAccessModifier", "UseUnderscoresInNumericLiterals",
    "UseDiamondOperator", "NoPackage"
]

occurrences = [
    2987, 1711, 2, 211, 749, 18, 10, 18, 157, 1,
    54, 2, 32, 1, 2, 553, 35, 10, 38, 307,
    14, 59, 1, 2790, 2, 4, 63, 30, 7, 
    353, 98, 3, 20
]
print(sum(occurrences))
# Creating the bar chart
plt.figure(figsize=(18, 9))
bars = plt.bar(warning_types, occurrences, color='skyblue')
plt.xlabel('Warning Types')
plt.ylabel('Occurrences')
plt.title('Static Analysis Warnings')
plt.xticks(rotation=90)

# Adding occurrences on top of the bars
for bar in bars:
    yval = bar.get_height()
    plt.text(bar.get_x() + bar.get_width() / 2, yval, int(yval), 
             ha='center', va='bottom', fontsize=10)

plt.tight_layout()
plt.show()
