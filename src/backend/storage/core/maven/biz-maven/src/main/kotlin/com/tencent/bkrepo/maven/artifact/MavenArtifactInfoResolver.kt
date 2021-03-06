/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.  
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package com.tencent.bkrepo.maven.artifact

import com.tencent.bkrepo.common.artifact.resolve.path.ArtifactInfoResolver
import com.tencent.bkrepo.common.artifact.resolve.path.Resolver
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

@Resolver(MavenArtifactInfo::class)
class MavenArtifactInfoResolver : ArtifactInfoResolver {
    override fun resolve(projectId: String, repoName: String, artifactUri: String, request: HttpServletRequest): MavenArtifactInfo {
        val mavenArtifactInfo = MavenArtifactInfo(projectId, repoName, artifactUri)
        // 仅当上传jar包时处理metadata
        if (artifactUri.endsWith(".jar")) {
            val paths = artifactUri.split("/")
            if (paths.size < pathMinLimit) {
                logger.debug(
                    "Cannot build MavenArtifactInfo from '{}'. The groupId, artifactId and version are unreadable.",
                    artifactUri
                )
                return MavenArtifactInfo("", "", "")
            }
            var pos = paths.size - groupMark

            mavenArtifactInfo.versionId = paths[pos--]
            mavenArtifactInfo.artifactId = paths[pos--]
            val stringBuilder = StringBuilder()
            while (pos >= 0) {
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.insert(0, '.')
                }
                stringBuilder.insert(0, paths[pos--])
            }
            mavenArtifactInfo.groupId = stringBuilder.toString()

            require(mavenArtifactInfo.isValid()) {
                throw IllegalArgumentException("Invalid unit info for '${mavenArtifactInfo.artifactUri}'.")
            }
        }
        return mavenArtifactInfo
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MavenArtifactInfoResolver::class.java)
        // artifact uri 最少请求参数 group/artifact/[version]/filename
        private const val pathMinLimit = 3
        private const val groupMark = 2
    }
}
