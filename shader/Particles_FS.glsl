#version 150

#include "shader/ShaderGlobals.glslh"

in vec2 fs_in_tc;
in vec3 fs_in_normal;
in vec4 fs_in_ViewPos;

out vec4 fs_out_color;

void main()
{
    vec2 tx = 2 * fs_in_tc - 1;
    if(tx.x * tx.x + tx.y * tx.y > 0.5)
    {
       discard;
    }
    vec3 normal = normalize(vec3(tx.x, -tx.y, -1));
    
    vec3 worldView = fs_in_ViewPos.xyz;
    
    vec3 lightPos = (g_view * vec4(g_lightPos, 1)).xyz;
    
    vec3 eye = (g_view * vec4(g_eye.xyz,1)).xyz;
    
    vec2 cofs = getSpecDiffuseCoe(normal, eye, worldView, lightPos);
    
    fs_out_color = (cofs.x * vec4(1) + cofs.y * g_color + vec4(1) * g_ambient);
    fs_out_color.w = 1;
}