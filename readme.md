GCCEL（Graph-based Chinese Colletive Entity Linking ）代码使用说明：
  GCCEL是针对中文实体链接的方法系统，使用基于图的方法实现，在GCCEL中，有几个模块是比较重要的：
  1.连接知识库的模块 Neo4jOperate.java
  针对这个模块，需要安装neo4j作为自己的本地知识库，然后将清华大学的知识库（http://keg.cs.tsinghua.edu.cn/project/ChineseKB）的内容导入到neo4j
  然后在Neo4jOperate.java配置本地知识库的路径。
  2.实体识别模块：EntityRecogtion.java
  针对这个模块，GCCEL中使用的中科院发布的64位的NLPIR工具作为实体识别工具，可以在Text/seguserDict.txt中添加自己的词汇，提高实体识别的召回率。
  3.构建图的模块 Graph/Graph.java
  针对这个模块，GCCEL构造的是有向图，在链接阶段基于了图的拓扑结构计算节点的出入度。
  4.爬虫模块 NetProcess/.
  主要是为GCCEL增量证据挖掘算法服务的，针对在本地知识库中不存在关系的实体，利用该模块去互动百科页面挖掘他们的关系。
  5.实体链接模块：NEntityLinking.java
  该模块是完成整个实体链接操作的主函数，直接调用该模块即可实现对文本中实体链接操作，其中实体置信度的阀值时可以调整，本算法设定的是0.35
