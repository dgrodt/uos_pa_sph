#version 330

#include "shader/ShaderGlobals.glslh"

in vec2 fs_in_tc;
in vec3 fs_in_normal;
in vec4 fs_in_ViewPos;
in float fs_in_depth;
in vec3 fs_in_worldPos;


//out vec4 fs_out_color;
layout(location = 0) out vec4 fs_out_color;
layout(location = 1) out float fs_out_depth;
layout(location = 2) out vec3 fs_out_worldPos;
layout(location = 3) out vec3 fs_out_normals;
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
    
    //fs_out_color = (cofs.x * vec4(0.1) + cofs.y * g_color + vec4(1) * g_ambient);
    //fs_out_color.x = clamp(fs_out_color.x, 0.3, 1);
    //fs_out_color.y = clamp(fs_out_color.y, 0.3, 1);
    //fs_out_color.z = clamp(fs_out_color.z, 0.5, 1);
    //fs_out_color.w = 1;
    fs_out_color = g_color;
    
    fs_out_depth = fs_in_depth;
    fs_out_worldPos = fs_in_worldPos;
    fs_out_normals = (cofs.x * vec4(0.1) + cofs.y * vec4(0.1)).xyz;//(cofs.x * vec4(1) + cofs.y * g_color + vec4(1) * g_ambient).xyz;
}