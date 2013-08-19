#version 150

#include "shader/ShaderGlobals.glslh"

uniform float circleCnt;

in vec3 vs_in_pos;
in vec3 vs_in_normal;
in vec2 vs_in_tc;
in vec4 vs_in_instance;
in vec4 vs_in_velos;

out vec2 fs_in_tc;
out vec3 fs_in_normal;
out vec4 fs_in_ViewPos;
out vec4 gl_Position;

#define PI 3.14159265358979323846f

void main()
{
    fs_in_normal = vs_in_normal; //g_model
    fs_in_tc = vs_in_tc;
    
    vec4 vPos = vec4(0);

    vec3 dir = normalize(vs_in_velos.xyz);
    
    float phi = atan(dir.x, dir.z);
    float theta = -0.5*PI + acos(dir.y);

    float sinPhi = sin(phi);
    float cosPhi = cos(phi);
    float sinTheta = sin(theta);
    float cosTheta = cos(theta);

    mat3x3 rot = mat3x3(
                        cosPhi, 0, -sinPhi,
                        sinPhi*sinTheta, cosTheta, cosPhi*sinTheta,
                        sinPhi*cosTheta, -sinTheta, cosPhi*cosTheta
                        );

    vPos.xyz = rot * vs_in_pos.xyz;
    float scale = 0.75f;
    vPos *= scale;
    
    vPos = vec4(vPos.xyz, 1);
    
    fs_in_ViewPos = g_view * vec4(vPos.xyz + vs_in_instance.xyz, 1);
    gl_Position = g_projection * fs_in_ViewPos;
}